package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayAndStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ProfileReset
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResetReason
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.JwtAuthHelper
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class ResettlementAssessmentResetServiceTest {
  private lateinit var resettlementAssessmentResetService: ResettlementAssessmentResetService

  @Mock
  private lateinit var resettlementAssessmentService: ResettlementAssessmentService

  @Mock
  private lateinit var caseNotesService: CaseNotesService

  @Mock
  private lateinit var pathwayAndStatusService: PathwayAndStatusService

  @Mock
  private lateinit var supportNeedsLegacyProfileService: SupportNeedsLegacyProfileService

  @BeforeEach
  fun beforeEach() {
    resettlementAssessmentResetService = ResettlementAssessmentResetService(
      resettlementAssessmentService,
      caseNotesService,
      pathwayAndStatusService,
      supportNeedsLegacyProfileService,
    )
  }

  @Test
  fun `test deleteAllResettlementAssessments - no prisoner`() {
    val nomsId = "12345"
    val auth = JwtAuthHelper().createJwt("J_DOE", authSource = "NOMIS")
    val profileReset = ProfileReset(
      resetReason = ResetReason.RECALL_TO_PRISON,
      additionalDetails = null,
    )

    whenever(resettlementAssessmentService.deleteAllResettlementAssessments(nomsId)).thenThrow(ResourceNotFoundException(""))

    assertThrows<ResourceNotFoundException> {
      resettlementAssessmentResetService.resetProfile(nomsId, profileReset, auth)
    }

    verify(resettlementAssessmentService).deleteAllResettlementAssessments(nomsId)
    verifyNoInteractions(caseNotesService)
    verifyNoInteractions(pathwayAndStatusService)
  }

  @Test
  fun `test deleteAllResettlementAssessments - happy path`() {
    val nomsId = "12345"
    val auth = JwtAuthHelper().createJwt("J_DOE", authSource = "NOMIS")
    val profileReset = ProfileReset(
      resetReason = ResetReason.RECALL_TO_PRISON,
      additionalDetails = null,
    )

    resettlementAssessmentResetService.resetProfile(nomsId, profileReset, auth)

    verify(resettlementAssessmentService).deleteAllResettlementAssessments(nomsId)
    verify(caseNotesService).sendProfileResetCaseNote(nomsId, "J_DOE", "The person has been recalled to prison")
    Pathway.entries.forEach { verify(pathwayAndStatusService).updatePathwayStatus(nomsId, PathwayAndStatus(it, Status.NOT_STARTED)) }
  }

  @ParameterizedTest
  @MethodSource("test getReason data")
  fun `test getReason`(profileReset: ProfileReset, expectedReason: String?, error: Boolean) {
    if (!error) {
      Assertions.assertEquals(expectedReason, resettlementAssessmentResetService.getReason(profileReset))
    } else {
      val exception = assertThrows<ServerWebInputException> { resettlementAssessmentResetService.getReason(profileReset) }
      Assertions.assertEquals("Either resetReason should not be OTHER and additionalDetails should be null or resetReason should be OTHER and additionalDetails should be null", exception.reason)
    }
  }

  private fun `test getReason data`() = Stream.of(
    Arguments.of(ProfileReset(ResetReason.RECALL_TO_PRISON, null), "The person has been recalled to prison", false),
    Arguments.of(ProfileReset(ResetReason.RETURN_ON_NEW_SENTENCE, null), "The person has returned to prison on a new sentence", false),
    Arguments.of(ProfileReset(ResetReason.OTHER, "This is the reason"), "This is the reason", false),
    Arguments.of(ProfileReset(ResetReason.OTHER, null), null, true),
    Arguments.of(ProfileReset(ResetReason.RECALL_TO_PRISON, "This is the reason"), null, true),
    Arguments.of(ProfileReset(ResetReason.RETURN_ON_NEW_SENTENCE, "This is the reason"), null, true),
  )
}
