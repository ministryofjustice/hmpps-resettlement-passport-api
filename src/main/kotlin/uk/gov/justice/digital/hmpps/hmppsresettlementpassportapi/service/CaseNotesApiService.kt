package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.CaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.CaseNotes
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.PathwayMap
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class CaseNotesApiService(
  private val offenderCaseNotesWebClientCredentials: WebClient,
) {

  suspend fun getCaseNotesByNomisId(
    nomisId: String,
    pageNumber: Int,
    pageSize: Int,
    sort: String,
    days: Int,
  ): CaseNotesList {
    if (nomisId.isBlank() || nomisId.isEmpty()) {
      throw NoDataWithCodeFoundException("Prisoner", nomisId)
    }

    var sortValue = "occurenceDateTime,DESC"
    if (!sort.isBlank() && !sort.isEmpty()) {
      sortValue = sort
    }

    if (pageNumber < 0 || pageSize <= 0) {
      throw NoDataWithCodeFoundException(
        "Data",
        "Page $pageNumber and Size $pageSize",
      )
    }
    val offendersCaseNotes = mutableListOf<CaseNote>()
    // TODO "REPORTS" Need to be replace with "GEN" and searchSubTerm to be "RESET"
    val caseNotesGEN = fetchCaseNotesByNomisId("REPORTS", "REP_IEP", nomisId, days)
    // TODO "GEN" Need to be replace with "RESET"
    val caseNotesRESET = fetchCaseNotesByNomisId("GEN", null, nomisId, days)
    caseNotesGEN.collect {
      offendersCaseNotes.addAll(it)
    }
    caseNotesRESET.collect {
      offendersCaseNotes.addAll(it)
    }
    if (offendersCaseNotes.isEmpty()) {
      throw NoDataWithCodeFoundException("Prisoner", nomisId)
    }

    val startIndex = (pageNumber * pageSize)
    if (startIndex >= offendersCaseNotes.size) {
      throw NoDataWithCodeFoundException(
        "Data",
        "Page $pageNumber",
      )
    }

    when (sortValue) {
      "occurenceDateTime,ASC" -> offendersCaseNotes.sortBy { it.occurrenceDateTime }
      "pathway,ASC" -> offendersCaseNotes.sortBy { it.subType }
      "occurenceDateTime,asc" -> offendersCaseNotes.sortBy { it.occurrenceDateTime }
      "pathway,asc" -> offendersCaseNotes.sortBy { it.subType }
      "occurenceDateTime,DESC" -> offendersCaseNotes.sortByDescending { it.occurrenceDateTime }
      "pathway,DESC" -> offendersCaseNotes.sortByDescending { it.subType }
      "occurenceDateTime,desc" -> offendersCaseNotes.sortByDescending { it.occurrenceDateTime }
      "pathway,desc" -> offendersCaseNotes.sortByDescending { it.subType }
      else -> throw NoDataWithCodeFoundException(
        "Data",
        "Sort value Invalid",
      )
    }

    val endIndex = (pageNumber * pageSize) + (pageSize)
    if (startIndex < endIndex && endIndex <= offendersCaseNotes.size) {
      val caseNotesPageList = offendersCaseNotes.subList(startIndex, endIndex)
      val cnList: List<PathwayCaseNote> = objectMapper(caseNotesPageList)
      return CaseNotesList(cnList, cnList.toList().size, pageNumber, sort, offendersCaseNotes.size, endIndex == offendersCaseNotes.size)
    } else if (startIndex < endIndex) {
      val caseNotesPageList = offendersCaseNotes.subList(startIndex, offendersCaseNotes.size)
      val cnList: List<PathwayCaseNote> = objectMapper(caseNotesPageList)
      return CaseNotesList(cnList, cnList.toList().size, pageNumber, sort, offendersCaseNotes.size, true)
    }

    return CaseNotesList(null, null, null, null, 0, false)
  }

  private suspend fun fetchCaseNotesByNomisId(searchTerm: String, searchSubTerm: String?, nomisId: String, days: Int): Flow<List<CaseNote>> = flow {
    var page = 0
    var uriValue = "/case-notes/{nomisId}?page={page}&size={size}&type={type}"
    val pattern = DateTimeFormatter.ISO_LOCAL_DATE_TIME // ofPattern(DateTimeFormatter.ISO_LOCAL_DATE_TIME.toString())
    val startDate = LocalDate.now().minusDays(days.toLong()).atStartOfDay().format(pattern)
    val endDate = LocalDate.now().plusDays(1).atStartOfDay().format(pattern)
    if (days != 0) {
      uriValue = "/case-notes/{nomisId}?page={page}&size={size}&type={type}&startDate={startDate}&endDate={endDate}"
    }
    if (searchSubTerm != null && days != 0) {
      uriValue = "/case-notes/{nomisId}?page={page}&size={size}&type={type}&subType={subType}&startDate={startDate}&endDate={endDate}"
    } else if (searchSubTerm != null) {
      uriValue = "/case-notes/{nomisId}?page={page}&size={size}&type={type}&subType={subType}"
    }

    do {
      val data = offenderCaseNotesWebClientCredentials.get()
        .uri(
          uriValue,
          mapOf(
            "nomisId" to nomisId,
            "size" to 500, // NB: API allows up 3,000 results per page
            "page" to page,
            "type" to searchTerm,
            "subType" to searchSubTerm,
            "startDate" to startDate,
            "endDate" to endDate,
          ),
        )
        .retrieve().onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("PrisonerId 2 $nomisId not found") })
      val pageOfData = data.awaitBodyOrNull<CaseNotes>()
      if (pageOfData != null) {
        emit(pageOfData.content!!)
      }
      page += 1
    } while (!pageOfData?.last!!)
  }

  private fun objectMapper(searchList: List<CaseNote>): List<PathwayCaseNote> {
    val caseNotesList = mutableListOf<PathwayCaseNote>()
    searchList.forEach { caseNote ->
      val prisoner = PathwayCaseNote(
        caseNote.caseNoteId,
        PathwayMap.valueOf(caseNote.subType).id,
        caseNote.creationDateTime,
        caseNote.occurrenceDateTime,
        caseNote.authorName,
        caseNote.text,
      )

      caseNotesList.add(prisoner)
    }
    return caseNotesList
  }
}
