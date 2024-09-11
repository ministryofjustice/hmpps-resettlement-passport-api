package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit

import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.getClaimFromJWTToken
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditService

@Component
class AuditService(private val auditService: HmppsAuditService) {
  fun audit(what: AuditAction, nomsId: String, auth: String, vararg details: String) {
    val userName = getClaimFromJWTToken(auth, "name") ?: throw ServerWebInputException("Cannot get name from auth token")

    runBlocking {
      auditService.publishEvent(
        what = what.name,
        who = userName,
        subjectId = nomsId,
        subjectType = "USER_ID",
        service = "hmpps-resettlement-passport-api",
        details = details.firstOrNull(),
      )
    }
  }
}

enum class AuditAction {
  COMPLETE_ASSESSMENT,
  GET_ASSESSMENT,
  GET_ASSESSMENT_SUMMARY,
  SUBMIT_ASSESSMENT,
}
