package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitBodyOrNull
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesMeta
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.CaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.CaseNotes
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.PATHWAY_PARENT_TYPE
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.PathwayMap
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class OffenderCaseNotesApiService(
  private val offenderCaseNotesWebClientCredentials: WebClient,
  private val offenderCaseNotesWebClientUserCredentials: WebClient,
  private val offenderSearchApiService: OffenderSearchApiService,
) {

  suspend fun getCaseNotesByNomsId(
    nomsId: String,
    pageNumber: Int,
    pageSize: Int,
    sort: String,
    days: Int,
    pathwayType: String,
    createdBy: Int,
  ): CaseNotesList {
    if (nomsId.isBlank()) {
      throw NoDataWithCodeFoundException("Prisoner", nomsId)
    }

    var sortValue = "occurenceDateTime,DESC"
    if (sort.isNotBlank()) {
      sortValue = sort
    }

    if (pageNumber < 0 || pageSize <= 0) {
      throw NoDataWithCodeFoundException(
        "Data",
        "Page $pageNumber and Size $pageSize",
      )
    }
    val pathwayValues = PathwayMap.values()
    val offendersCaseNotes = mutableListOf<CaseNote>()
    if (pathwayType == "All") {
      val caseNotesGEN = fetchCaseNotesByNomsId("GEN", "RESET", nomsId, days)
      val caseNotesRESET = fetchCaseNotesByNomsId("RESET", null, nomsId, days)
      caseNotesGEN.collect {
        offendersCaseNotes.addAll(it)
      }
      caseNotesRESET.collect {
        offendersCaseNotes.addAll(it)
      }
    } else if (pathwayValues.any { it.id == pathwayType }) {
      if (pathwayType == "GENERAL") {
        val caseNotesGEN = fetchCaseNotesByNomsId("GEN", "RESET", nomsId, days)
        if (createdBy != 0) {
          caseNotesGEN.collect { caseNoteList ->
            caseNoteList.forEach {
              if (it.authorUserId.toInt() == createdBy) {
                offendersCaseNotes.add(it)
              }
            }
          }
        } else {
          caseNotesGEN.collect {
            offendersCaseNotes.addAll(it)
          }
        }
      } else {
        val pathwayVal = pathwayValues.find { it.id == pathwayType }
        val caseNotesRESET = fetchCaseNotesByNomsId("RESET", pathwayVal?.name, nomsId, days)
        if (createdBy != 0) {
          caseNotesRESET.collect { caseNoteList ->
            caseNoteList.forEach {
              if (it.authorUserId.toInt() == createdBy) {
                offendersCaseNotes.add(it)
              }
            }
          }
        } else {
          caseNotesRESET.collect {
            offendersCaseNotes.addAll(it)
          }
        }
      }
    } else {
      throw NoDataWithCodeFoundException(
        "Data",
        "PathwayType $pathwayType Invalid",
      )
    }
    if (offendersCaseNotes.isEmpty()) {
      throw NoDataWithCodeFoundException("Prisoner", nomsId)
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
      return CaseNotesList(
        cnList,
        cnList.toList().size,
        pageNumber,
        sort,
        offendersCaseNotes.size,
        endIndex == offendersCaseNotes.size,
      )
    } else if (startIndex < endIndex) {
      val caseNotesPageList = offendersCaseNotes.subList(startIndex, offendersCaseNotes.size)
      val cnList: List<PathwayCaseNote> = objectMapper(caseNotesPageList)
      return CaseNotesList(cnList, cnList.toList().size, pageNumber, sort, offendersCaseNotes.size, true)
    }

    return CaseNotesList(null, null, null, null, 0, false)
  }

  private suspend fun fetchCaseNotesByNomsId(
    searchTerm: String,
    searchSubTerm: String?,
    nomsId: String,
    days: Int,
  ): Flow<List<CaseNote>> = flow {
    var page = 0
    var uriValue = "/case-notes/{nomsId}?page={page}&size={size}&type={type}"
    val pattern = DateTimeFormatter.ISO_LOCAL_DATE_TIME // ofPattern(DateTimeFormatter.ISO_LOCAL_DATE_TIME.toString())
    val startDate = LocalDate.now().minusDays(days.toLong()).atStartOfDay().format(pattern)
    val endDate = LocalDate.now().plusDays(1).atStartOfDay().format(pattern)
    if (days != 0) {
      uriValue = "/case-notes/{nomsId}?page={page}&size={size}&type={type}&startDate={startDate}&endDate={endDate}"
    }
    if (searchSubTerm != null && days != 0) {
      uriValue =
        "/case-notes/{nomsId}?page={page}&size={size}&type={type}&subType={subType}&startDate={startDate}&endDate={endDate}"
    } else if (searchSubTerm != null) {
      uriValue = "/case-notes/{nomsId}?page={page}&size={size}&type={type}&subType={subType}"
    }
    do {
      val data = offenderCaseNotesWebClientCredentials.get()
        .uri(
          uriValue,
          mapOf(
            "nomsId" to nomsId,
            "size" to 500, // NB: API allows up 3,000 results per page
            "page" to page,
            "type" to searchTerm,
            "subType" to searchSubTerm,
            "startDate" to startDate,
            "endDate" to endDate,
          ),
        )
        .retrieve()
        .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("PrisonerId $nomsId not found and the uri is $uriValue") })
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

  suspend fun getCaseNotesCreatorsByPathway(nomsId: String, pathwayType: String): List<CaseNotesMeta> {
    val type: String
    val subType: String
    val pathwayValues = PathwayMap.values()
    if (pathwayValues.any { it.id == pathwayType }) {
      if (pathwayType == "GENERAL") {
        type = "GEN"
        subType = "RESET"
      } else {
        type = "RESET"
        val pathwayVal = pathwayValues.find { it.id == pathwayType }
        subType = pathwayVal?.name.toString()
      }
      val offendersCaseNotes = mutableListOf<CaseNote>()
      val creatorsList = mutableListOf<CaseNotesMeta>()
      val caseNotes = fetchCaseNotesByNomsId(type, subType, nomsId, 0)
      caseNotes.collect {
        offendersCaseNotes.addAll(it)
      }
      offendersCaseNotes.forEach { caseNote ->
        val casenoteMeta = CaseNotesMeta(
          caseNote.authorName,
          caseNote.authorUserId,
        )
        creatorsList.add(casenoteMeta)
      }
      return creatorsList.distinct()
    } else {
      throw NoDataWithCodeFoundException(
        "Data",
        "PathwayType $pathwayType Invalid",
      )
    }
  }
  suspend fun postCaseNote(nomsId: String, caseNotes: CaseNotesRequest, auth: String): CaseNote {
    val type = PATHWAY_PARENT_TYPE
    val pathwayValues = PathwayMap.values()
    val pathwayVal = pathwayValues.find { it.id == caseNotes.pathway }
    val subType = pathwayVal?.name.toString()
    val prisonCode = offenderSearchApiService.findPrisonerPersonalDetails(nomsId).prisonId

    return offenderCaseNotesWebClientUserCredentials.post()
      .uri(
        "/case-notes/{nomsId}",
        nomsId,
      ).contentType(MediaType.APPLICATION_JSON)
      .header(HttpHeaders.AUTHORIZATION, auth)
      .bodyValue(
        mapOf(
          "locationId" to prisonCode,
          "type" to type,
          "subType" to subType,
          "text" to caseNotes.text,
        ),
      )
      .retrieve()
      .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("Prisoner $nomsId not found") })
      .awaitBody<CaseNote>()
  }
}