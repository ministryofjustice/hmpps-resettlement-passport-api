package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerNeedsRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedsUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.SupportNeedsService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditAction
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit.AuditService

class SupportNeedsResourceControllerTest {

  @Mock
  private lateinit var supportNeedsService: SupportNeedsService

  @Mock
  private lateinit var auditService: AuditService

  private lateinit var supportNeedsResourceController: SupportNeedsResourceController

  private var jwtAuthHelper = JwtAuthHelper()

  @BeforeEach
  fun beforeEach() {
    MockitoAnnotations.openMocks(this)
    supportNeedsResourceController = SupportNeedsResourceController(supportNeedsService, auditService)
  }

  @Test
  fun postPrisonerNeedsById() {
  }

  @Test
  fun `Post prisoner needs by Id - Check Audit call`() {
    val nomsId = "G4161UF"
    val request = PrisonerNeedsRequest(listOf())
    val token = jwtAuthHelper.createJwt("John Doe", authSource = "NOMIS")
    val auth = "Bearer $token"

    supportNeedsResourceController.postPrisonerNeedsById(nomsId, request, auth)

    verify(auditService).audit(AuditAction.SUBMIT_SUPPORT_NEEDS, nomsId, auth)
  }

  @Test
  fun `Patch prisoner needs by Id - Check Audit call`() {
    val nomsId = "G4161UF"
    val prisonerNeedId = 12345L
    val request = SupportNeedsUpdateRequest("some text", SupportNeedStatus.IN_PROGRESS, true, false)
    val token = jwtAuthHelper.createJwt("John Doe", authSource = "NOMIS")
    val auth = "Bearer $token"

    supportNeedsResourceController.patchSupportNeedById(nomsId, prisonerNeedId, request, auth)

    verify(auditService).audit(AuditAction.UPDATE_SUPPORT_NEED, nomsId, auth)
  }
}
