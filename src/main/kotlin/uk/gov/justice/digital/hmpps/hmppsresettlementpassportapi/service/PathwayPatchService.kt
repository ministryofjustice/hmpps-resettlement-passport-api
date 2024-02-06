package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayAndStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayStatusAndCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.CaseNotesApiService

@Service
class PathwayPatchService(
  private val caseNotesApiService: CaseNotesApiService,
  private val pathwayAndStatusService: PathwayAndStatusService,
  private val deliusContactService: DeliusContactService,
) {

  @Transactional
  fun updatePathwayStatusWithCaseNote(
    nomsId: String,
    pathwayStatusAndCaseNote: PathwayStatusAndCaseNote,
    auth: String,
  ): ResponseEntity<Void> {
    pathwayAndStatusService.updatePathwayStatus(
      nomsId,
      PathwayAndStatus(pathwayStatusAndCaseNote.pathway, pathwayStatusAndCaseNote.status),
    )
    // If this is a Nomis user we use the Case Notes API, if this is a Delius user then store in database for now -
    // in the future we will also push them to Delius.
    val authSource = getClaimFromJWTToken(auth, "auth_source")?.lowercase()
    val userId = getClaimFromJWTToken(auth, "sub") ?: throw ServerWebInputException("Cannot get sub from auth token")

    when (authSource) {
      "nomis" -> {
        caseNotesApiService.postCaseNote(
          nomsId,
          pathwayStatusAndCaseNote.pathway,
          pathwayStatusAndCaseNote.caseNoteText,
          userId,
        )
      }
      "delius" -> {
        val name = getClaimFromJWTToken(auth, "name") ?: throw ServerWebInputException("JWT token must include a claim for 'name'")
        deliusContactService.addDeliusCaseNoteToDatabase(nomsId, pathwayStatusAndCaseNote, name)
      }
      else -> {
        throw ServerWebInputException("JWT token must include a claim for 'auth_source' as either 'nomis' or 'delius'")
      }
    }

    return ResponseEntity.ok().build()
  }
}
