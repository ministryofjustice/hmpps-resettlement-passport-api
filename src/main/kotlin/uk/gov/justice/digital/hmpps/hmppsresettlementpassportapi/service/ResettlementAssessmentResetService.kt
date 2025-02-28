package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayAndStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ProfileReset
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResetReason
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status

@Service
class ResettlementAssessmentResetService(
  private val resettlementAssessmentService: ResettlementAssessmentService,
  private val caseNotesService: CaseNotesService,
  private val pathwayAndStatusService: PathwayAndStatusService,
  private val supportNeedsLegacyProfileService: SupportNeedsLegacyProfileService,
) {

  @Transactional
  fun resetProfile(nomsId: String, profileReset: ProfileReset, auth: String, supportNeedsEnabled: Boolean) {
    val authSource = getClaimFromJWTToken(auth, "auth_source")?.lowercase()
    if (authSource != "nomis") {
      throw ServerWebInputException("Endpoint must be called with a user token with authSource of NOMIS")
    }
    val userId = getClaimFromJWTToken(auth, "sub") ?: throw ServerWebInputException("Cannot get sub from auth token")

    val reason = getReason(profileReset)

    resettlementAssessmentService.deleteAllResettlementAssessments(nomsId)
    Pathway.entries.forEach { pathway -> pathwayAndStatusService.updatePathwayStatus(nomsId, PathwayAndStatus(pathway, Status.NOT_STARTED)) }
    caseNotesService.sendProfileResetCaseNote(nomsId, userId, reason, supportNeedsEnabled)

    // Set the supportNeedsLegacyProfile to false
    supportNeedsLegacyProfileService.setSupportNeedsLegacyFlag(nomsId, false)
  }

  fun getReason(profileReset: ProfileReset): String {
    // There shouldn't be additional details unless the reason is OTHER
    if ((profileReset.resetReason == ResetReason.OTHER && profileReset.additionalDetails == null) || (profileReset.resetReason != ResetReason.OTHER && profileReset.additionalDetails != null)) {
      throw ServerWebInputException("Either resetReason should not be OTHER and additionalDetails should be null or resetReason should be OTHER and additionalDetails should be null")
    }

    return when (profileReset.resetReason) {
      ResetReason.RECALL_TO_PRISON -> "The person has been recalled to prison"
      ResetReason.RETURN_ON_NEW_SENTENCE -> "The person has returned to prison on a new sentence"
      ResetReason.OTHER -> profileReset.additionalDetails!!
    }
  }
}
