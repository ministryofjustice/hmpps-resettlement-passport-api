package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.CaseNotes
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.PathwayMap

@Service
class CaseNotesApiService(
  private val offenderCaseNotesWebClientCredentials: WebClient,
) {

  suspend fun getCaseNotesByNomisId(
    nomisId: String,
    pageNumber: Int,
    pageSize: Int,
    sort: String,
  ): CaseNotesList {
    if (nomisId.isBlank() || nomisId.isEmpty()) {
      throw NoDataWithCodeFoundException("Prisoner", nomisId)
    }

    if (pageNumber < 0 || pageSize <= 0) {
      throw NoDataWithCodeFoundException(
        "Data",
        "Page $pageNumber and Size $pageSize",
      )
    }

    val caseNotes = fetchCaseNotesByNomisId(nomisId, pageNumber, pageSize, sort)
    val content = mutableListOf<PathwayCaseNote>()
    caseNotes?.content?.forEach {
      // var pathwayType = it.subType
      val pathwayCaseNote = PathwayCaseNote(it.caseNoteId, PathwayMap.valueOf(it.subType).id, it.creationDateTime, it.occurrenceDateTime, it.authorName, it.text)
      content.add(pathwayCaseNote)
    }

    if (caseNotes != null) {
      return CaseNotesList(content, caseNotes.size, caseNotes.number, sort, caseNotes.totalElements, caseNotes.last)
    } else {
      return CaseNotesList(null, pageSize, pageNumber, sort, 0, true)
    }
  }

  private suspend fun fetchCaseNotesByNomisId(nomisId: String, pageNumber: Int, pageSize: Int, sortBy: String): CaseNotes? {
    val data = offenderCaseNotesWebClientCredentials.get()
      .uri(
        "/case-notes/{nomisId}?page={page}&size={size}&sort={sort}&type={type}",
        mapOf(
          "nomisId" to nomisId,
          "size" to pageSize, // NB: API allows up 3,000 results per page
          "page" to pageNumber,
          "sort" to sortBy,
          "type" to "GEN",
        ),
      )
      .retrieve().onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("PrisonerId $nomisId not found") })

    return data.awaitBodyOrNull<CaseNotes>()
  }
}
