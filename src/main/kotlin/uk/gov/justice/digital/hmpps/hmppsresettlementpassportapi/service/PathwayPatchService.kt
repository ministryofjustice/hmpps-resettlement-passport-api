package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayAndStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayStatusAndCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.OffenderCaseNotesApiService

@Service
class PathwayPatchService(
  private val offenderCaseNotesApiService: OffenderCaseNotesApiService,
  private val pathwayAndStatusService: PathwayAndStatusService,
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
    offenderCaseNotesApiService.postCaseNote(
      nomsId,
      pathwayStatusAndCaseNote.pathway,
      pathwayStatusAndCaseNote.caseNoteText,
      auth,
    )
    return ResponseEntity.ok().build()
  }
}
