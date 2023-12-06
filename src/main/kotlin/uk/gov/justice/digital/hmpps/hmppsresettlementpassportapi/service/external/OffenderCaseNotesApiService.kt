package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesMeta
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.CaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.CaseNotes
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.PATHWAY_PARENT_TYPE
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.PathwayMap
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.isAllowedSubTypes
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class OffenderCaseNotesApiService(
  private val offenderCaseNotesWebClientUserCredentials: WebClient,
  private val offenderCaseNotesWebClientCredentials: WebClient,
  private val offenderSearchApiService: OffenderSearchApiService,

) {

  fun getCaseNotesByNomsId(
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
      offendersCaseNotes.addAll(caseNotesGEN)
      offendersCaseNotes.addAll(caseNotesRESET)
    } else if (pathwayValues.any { it.id == pathwayType }) {
      if (pathwayType == "GENERAL") {
        val caseNotesGEN = fetchCaseNotesByNomsId("GEN", "RESET", nomsId, days)
        if (createdBy != 0) {
          caseNotesGEN.forEach {
            if (it.authorUserId.toInt() == createdBy) {
              offendersCaseNotes.add(it)
            }
          }
        } else {
          offendersCaseNotes.addAll(caseNotesGEN)
        }
      } else {
        val pathwayVal = pathwayValues.find { it.id == pathwayType }
        val caseNotesRESET = fetchCaseNotesByNomsId("RESET", pathwayVal?.name, nomsId, days)
        if (createdBy != 0) {
          caseNotesRESET.forEach {
            if (it.authorUserId.toInt() == createdBy) {
              offendersCaseNotes.add(it)
            }
          }
        } else {
          offendersCaseNotes.addAll(caseNotesRESET)
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

  private fun fetchCaseNotesByNomsId(
    searchTerm: String,
    searchSubTerm: String?,
    nomsId: String,
    days: Int,
  ): List<CaseNote> {
    val listToReturn = mutableListOf<CaseNote>()

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
        .onStatus(
          { it == HttpStatus.NOT_FOUND },
          { throw ResourceNotFoundException("PrisonerId $nomsId not found and the uri is $uriValue") },
        )
      val pageOfData = data.bodyToMono<CaseNotes>().block()
      if (pageOfData != null) {
        listToReturn.addAll(pageOfData.content!!)
      }
      page += 1
    } while (!pageOfData?.last!!)

    return listToReturn
  }

  private fun objectMapper(searchList: List<CaseNote>): List<PathwayCaseNote> {
    val caseNotesList = mutableListOf<PathwayCaseNote>()
    var subType: String
    searchList.forEach { caseNote ->
      subType = if (isAllowedSubTypes(caseNote.subType)) {
        PathwayMap.valueOf(caseNote.subType).id
      } else {
        PathwayMap.GEN.id
      }
      val prisoner = PathwayCaseNote(
        caseNote.caseNoteId,
        subType,
        caseNote.creationDateTime,
        caseNote.occurrenceDateTime,
        caseNote.authorName,
        caseNote.text,
      )
      caseNotesList.add(prisoner)
    }
    return caseNotesList
  }

  fun getCaseNotesCreatorsByPathway(nomsId: String, pathwayType: String): List<CaseNotesMeta> {
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
      val creatorsList = mutableListOf<CaseNotesMeta>()
      val caseNotes = fetchCaseNotesByNomsId(type, subType, nomsId, 0)
      caseNotes.forEach { caseNote ->
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

  fun postCaseNote(nomsId: String, pathway: Pathway, caseNotesText: String, auth: String): CaseNote? {
    val type = PATHWAY_PARENT_TYPE
    val pathwayValues = PathwayMap.values()
    val pathwayVal = pathwayValues.find { it.id == pathway.name }
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
          "text" to caseNotesText,
        ),
      )
      .retrieve()
      .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("Prisoner $nomsId not found") })
      .bodyToMono<CaseNote>()
      .block()
  }
}
