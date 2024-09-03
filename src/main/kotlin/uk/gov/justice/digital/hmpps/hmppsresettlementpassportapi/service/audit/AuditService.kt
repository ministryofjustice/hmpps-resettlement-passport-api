package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit

import org.springframework.context.annotation.Import
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsauditsdk.AuditService as AuditSdkService

@Component
@Import(AuditSdkService::class)
class AuditService(private val auditService: AuditSdkService) {
  fun audit(what: AuditAction, nomsId: String, userName: String?) {
    auditService.publishEvent(
      what = what.name,
      who = userName,
      subjectId = nomsId,
      subjectType = "USER_ID",
      service = "hmpps-resettlement-passport-api",
    )
  }
}

enum class AuditAction {
  IMMEDIATE_NEEDS_REPORT_SUBMITTED_SUCCESS,
  PRE_RELEASE_REPORT_SUBMITTED_SUCCESS,
}
