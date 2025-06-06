package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.audit

import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.getClaimFromJWTToken
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditService

@Component
class AuditService(private val auditService: HmppsAuditService) {

  fun audit(what: AuditAction, nomsId: String, auth: String, vararg details: String?) {
    val userName = getClaimFromJWTToken(auth, "sub") ?: throw ServerWebInputException("Cannot get name from auth token")

    runBlocking {
      auditService.publishEvent(
        what = what.name,
        who = userName,
        subjectId = nomsId,
        subjectType = "PRISONER_ID",
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
  CREATE_APPOINTMENT,
  CREATE_ASSESSMENT,
  DELETE_ASSESSMENT,
  CREATE_BANK_APPLICATION,
  DELETE_BANK_APPLICATION,
  UPDATE_BANK_APPLICATION,
  CASE_ALLOCATION,
  CASE_UNALLOCATION,
  UPLOAD_DOCUMENT,
  DELETE_DOCUMENT,
  CREATE_ID_APPLICATION,
  DELETE_ID_APPLICATION,
  UPDATE_ID_APPLICATION,
  UPDATE_LICENCE_CONDITION_SEEN,
  UPDATE_PATHWAY_STATUS_WITH_CASE_NOTE,
  CREATE_PYF_USER_OTP,
  DELETE_PYF_USER_OTP,
  VERIFY_PYF_USER_BY_KNOWLEDGE_ANSWER,
  RESET_PROFILE,
  CREATE_TODO,
  DELETE_TODO,
  UPDATE_TODO,
  COMPLETE_TODO,
  SUBMIT_SUPPORT_NEEDS,
  UPDATE_SUPPORT_NEED,
}
