package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.getClaimFromJWTToken
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditService

@Component
class AuditService(private val auditService: HmppsAuditService) {
  private val log = LoggerFactory.getLogger(this::class.java)

  @Throws(Exception::class)
  fun audit(what: AuditAction, nomsId: String, auth: String, vararg details: String) {
    val userName = getClaimFromJWTToken(auth, "sub") ?: throw ServerWebInputException("Cannot get name from auth token")

    runBlocking {
      try {
        auditService.publishEvent(
          what = what.name,
          who = userName,
          subjectId = nomsId,
          subjectType = "PRISONER_ID",
          service = "hmpps-resettlement-passport-api",
          details = details.firstOrNull(),
        )
      } catch (e: Exception) {
        log.error("Unable to publish audit event", e)
        throw e
      }
    }
  }
}

enum class AuditAction {
  COMPLETE_ASSESSMENT,
  GET_ASSESSMENT,
  GET_ASSESSMENT_SUMMARY,
  SUBMIT_ASSESSMENT,
}
