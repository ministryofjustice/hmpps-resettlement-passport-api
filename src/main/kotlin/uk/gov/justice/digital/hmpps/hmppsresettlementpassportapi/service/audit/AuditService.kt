package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit

import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditService

@Component
class AuditService(private val auditService: HmppsAuditService) {
  fun audit(what: AuditAction, nomsId: String, userName: String) {
    runBlocking {
      auditService.publishEvent(
        what = what.name,
        who = userName,
        subjectId = nomsId,
        subjectType = "USER_ID",
        service = "hmpps-resettlement-passport-api",
      )
    }
  }
}

enum class AuditAction {
  IMMEDIATE_NEEDS_REPORT_SUBMITTED_SUCCESS,
  PRE_RELEASE_REPORT_SUBMITTED_SUCCESS,
}
