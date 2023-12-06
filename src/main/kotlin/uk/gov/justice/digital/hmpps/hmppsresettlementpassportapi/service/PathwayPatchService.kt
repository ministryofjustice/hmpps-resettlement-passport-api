package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.nimbusds.jwt.JWTParser
import jakarta.transaction.Transactional
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayAndStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayStatusAndCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.OffenderCaseNotesApiService

@Service
class PathwayPatchService(
  private val offenderCaseNotesApiService: OffenderCaseNotesApiService,
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
    val jwtClaimsSet = JWTParser.parse(auth.replaceFirst("Bearer ", "")).jwtClaimsSet
    val authSource = jwtClaimsSet.getStringClaim("auth_source").lowercase()

    when (authSource) {
      "nomis" -> {
        offenderCaseNotesApiService.postCaseNote(
          nomsId,
          pathwayStatusAndCaseNote.pathway,
          pathwayStatusAndCaseNote.caseNoteText,
          auth,
        )
      }
      "delius" -> {
        val name = jwtClaimsSet.getStringClaim("name") ?: throw ServerWebInputException("JWT token must include a claim for 'name'")
        deliusContactService.addDeliusCaseNoteToDatabase(nomsId, pathwayStatusAndCaseNote, name)
      }
      else -> {
        throw ServerWebInputException("JWT token must include a claim for 'auth_source' as either 'nomis' or 'delius'")
      }
    }

    return ResponseEntity.ok().build()
  }
}
