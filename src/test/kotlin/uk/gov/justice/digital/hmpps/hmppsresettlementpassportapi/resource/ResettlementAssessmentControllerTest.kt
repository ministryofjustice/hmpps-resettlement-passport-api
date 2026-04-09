package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentCompleteRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.CustomJwtAuthorisationHelper
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.ResettlementAssessmentService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditAction
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies.ResettlementAssessmentStrategy
import uk.gov.justice.hmpps.kotlin.auth.AuthSource

class ResettlementAssessmentControllerTest {
  @Mock
  private lateinit var resettlementAssessmentStrategy: ResettlementAssessmentStrategy

  @Mock
  private lateinit var resettlementAssessmentService: ResettlementAssessmentService

  @Mock
  private lateinit var auditService: AuditService

  private lateinit var resettlementAssessmentController: ResettlementAssessmentController

  private var jwtAuthorisationHelper = CustomJwtAuthorisationHelper()

  @BeforeEach
  fun beforeEach() {
    MockitoAnnotations.openMocks(this)

    resettlementAssessmentController = ResettlementAssessmentController(
      resettlementAssessmentStrategy,
      resettlementAssessmentService,
      auditService,
    )
  }

  @Test
  fun `Get resettlement assessment summary by noms ID - Check Audit call`() {
    val nomsId = "G4161UF"
    val assessmentType = ResettlementAssessmentType.BCST2
    val auth = makeAuth()
    val details = """
      {"assessmentType":"BCST2"}
    """.trim().trimIndent()

    resettlementAssessmentController.getResettlementAssessmentSummaryByNomsId(nomsId, assessmentType, auth)

    verify(auditService).audit(AuditAction.GET_ASSESSMENT_SUMMARY, nomsId, auth, details)
  }

  @Test
  fun `post Complete assessment by noms ID - Check Audit call`() {
    val nomsId = "G4161UF"
    val assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN
    val pathway = Pathway.HEALTH
    val request = ResettlementAssessmentCompleteRequest(questionsAndAnswers = listOf(), version = 1)
    val auth = makeAuth()
    val details = """
      {"assessmentType":"RESETTLEMENT_PLAN","pathway":"HEALTH"}
    """.trim().trimIndent()

    resettlementAssessmentController.postCompleteAssessmentByNomsId(nomsId, pathway, request, assessmentType, true, auth)

    verify(auditService).audit(AuditAction.COMPLETE_ASSESSMENT, nomsId, auth, details)
  }

  @Test
  fun `post submit assessment by noms ID - Check Audit call`() {
    val nomsId = "G4161UF"
    val assessmentType = ResettlementAssessmentType.BCST2
    val auth = makeAuth()
    val details = """
      {"assessmentType":"BCST2"}
    """.trim().trimIndent()

    resettlementAssessmentController.postSubmitAssessmentByNomsId(nomsId = nomsId, assessmentType = assessmentType, auth = auth)

    verify(auditService).audit(AuditAction.SUBMIT_ASSESSMENT, nomsId, auth, details)
  }

  @Test
  fun `get resettlement assessment by noms ID - Check Audit call`() {
    val nomsId = "G4161UF"
    val pathway = Pathway.HEALTH
    val auth = makeAuth()
    val details = """
      {"pathway":"HEALTH"}
    """.trim().trimIndent()

    resettlementAssessmentController.getResettlementAssessmentByNomsId(nomsId, pathway, auth)

    verify(auditService).audit(AuditAction.GET_ASSESSMENT, nomsId, auth, details)
  }

  private fun makeAuth() = jwtAuthorisationHelper.createJwtAccessToken(
    username = "john_doe",
    authSource = AuthSource.NOMIS.source,
    name = "John Doe",
  ).let { "Bearer $it" }
}
