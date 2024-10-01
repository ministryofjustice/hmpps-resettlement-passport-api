package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class ResettlementAssessmentResetServiceTest {
  private lateinit var resettlementAssessmentResetService: ResettlementAssessmentResetService

  @Mock
  private lateinit var resettlementAssessmentService: ResettlementAssessmentService

  @Mock
  private lateinit var pathwayPatchService: PathwayPatchService

  @BeforeEach
  fun beforeEach() {
    resettlementAssessmentResetService = ResettlementAssessmentResetService(
      resettlementAssessmentService,
      pathwayPatchService,
    )
  }

  @Test
  fun `test deleteAllResettlementAssessments - no prisoner`() {
    val nomsId = "12345"
    val auth = ""

    whenever(resettlementAssessmentService.deleteAllResettlementAssessments(nomsId)).thenThrow(ResourceNotFoundException(""))

    assertThrows<ResourceNotFoundException> {
      resettlementAssessmentResetService.resetProfile(nomsId, auth)
    }

    verify(resettlementAssessmentService).deleteAllResettlementAssessments(nomsId)
    verifyNoInteractions(pathwayPatchService)
  }

  @Test
  fun `test deleteAllResettlementAssessments - happy path`() {
    val nomsId = "12345"
    val auth = ""

    resettlementAssessmentResetService.resetProfile(nomsId, auth)

    verify(resettlementAssessmentService).deleteAllResettlementAssessments(nomsId)
    Pathway.entries.forEach { pathway ->
      verify(pathwayPatchService).updatePathwayStatusWithCaseNote(nomsId, getResetProfilePathwayStatusAndCaseNote(pathway), auth)
    }
  }
}
