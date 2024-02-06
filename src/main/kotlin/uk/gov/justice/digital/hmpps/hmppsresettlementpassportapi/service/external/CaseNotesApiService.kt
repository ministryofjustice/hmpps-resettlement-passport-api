package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientException
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotePathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNoteSubType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNoteType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesMeta
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.CaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.CaseNotes
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.CaseNotesClientCredentialsService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.enumIncludes
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class CaseNotesApiService(
  private val caseNotesWebClientCredentials: WebClient,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val caseNotesClientCredentialsService: CaseNotesClientCredentialsService,

) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getCaseNotesByNomsId(
    nomsId: String,
    days: Int,
    pathwayType: CaseNotePathway,
    createdBy: Int,
  ): List<PathwayCaseNote> {
    val caseNotes = mutableListOf<CaseNote>()
    if (pathwayType == CaseNotePathway.All) {
      val caseNotesGEN = fetchCaseNotesByNomsId(CaseNoteType.GEN, CaseNoteSubType.RESET, nomsId, days)
      val caseNotesRESET = fetchCaseNotesByNomsId(CaseNoteType.RESET, null, nomsId, days)
      caseNotes.addAll(caseNotesGEN)
      caseNotes.addAll(caseNotesRESET)
    } else {
      if (pathwayType == CaseNotePathway.GENERAL) {
        val caseNotesGEN = fetchCaseNotesByNomsId(CaseNoteType.GEN, CaseNoteSubType.RESET, nomsId, days)
        if (createdBy != 0) {
          caseNotesGEN.forEach {
            if (it.authorUserId.toInt() == createdBy) {
              caseNotes.add(it)
            }
          }
        } else {
          caseNotes.addAll(caseNotesGEN)
        }
      } else {
        val subType = convertCaseNotePathwayToCaseNoteSubType(pathwayType)
        val caseNotesRESET = fetchCaseNotesByNomsId(CaseNoteType.RESET, subType, nomsId, days)
        if (createdBy != 0) {
          caseNotesRESET.forEach {
            if (it.authorUserId.toInt() == createdBy) {
              caseNotes.add(it)
            }
          }
        } else {
          caseNotes.addAll(caseNotesRESET)
        }
      }
    }

    return mapCaseNotes(caseNotes)
  }

  private fun fetchCaseNotesByNomsId(
    type: CaseNoteType,
    subType: CaseNoteSubType?,
    nomsId: String,
    days: Int,
  ): List<CaseNote> {
    val listToReturn = mutableListOf<CaseNote>()

    var page = 0
    var uriValue = "/case-notes/{nomsId}?page={page}&size={size}&type={type}"
    val pattern = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val startDate = LocalDate.now().minusDays(days.toLong()).atStartOfDay().format(pattern)
    val endDate = LocalDate.now().plusDays(1).atStartOfDay().format(pattern)

    if (days != 0) {
      uriValue += "&startDate={startDate}&endDate={endDate}"
    }

    if (subType != null) {
      uriValue += "&subType={subType}"
    }

    do {
      val data = caseNotesWebClientCredentials.get()
        .uri(
          uriValue,
          mapOf(
            "nomsId" to nomsId,
            // NB: API allows up 3,000 results per page
            "size" to 500,
            "page" to page,
            "type" to type,
            "subType" to subType,
            "startDate" to startDate,
            "endDate" to endDate,
          ),
        )
        .retrieve()
      val pageOfData = data.bodyToMono<CaseNotes>().onErrorReturn(
        {
          log.warn("Unexpected error from Case Notes API - ignoring but NOMIS case notes will be missing from response!", it)
          it is WebClientException
        },
        CaseNotes(number = 0, first = false, last = true, empty = true, numberOfElements = 0, size = 0, totalPages = 0, totalElements = 0),
      ).block()
      if (pageOfData != null) {
        listToReturn.addAll(pageOfData.content!!)
      }
      page += 1
    } while (!pageOfData?.last!!)

    return listToReturn
  }

  private fun mapCaseNotes(searchList: List<CaseNote>): List<PathwayCaseNote> {
    val caseNotesList = mutableListOf<PathwayCaseNote>()
    searchList.forEach { caseNote ->
      val pathwayType = if (enumIncludes<CaseNoteSubType>(caseNote.subType)) {
        convertCaseNoteSubTypeToCaseNotePathway(CaseNoteSubType.valueOf(caseNote.subType))
      } else {
        CaseNotePathway.GENERAL
      }
      val prisoner = PathwayCaseNote(
        caseNote.caseNoteId,
        pathwayType,
        caseNote.creationDateTime,
        caseNote.occurrenceDateTime,
        caseNote.authorName,
        caseNote.text,
      )
      caseNotesList.add(prisoner)
    }
    return caseNotesList
  }

  fun getCaseNotesCreatorsByPathway(nomsId: String, pathwayType: CaseNotePathway): List<CaseNotesMeta> {
    val type: CaseNoteType
    val subType: CaseNoteSubType?
    if (pathwayType == CaseNotePathway.GENERAL) {
      type = CaseNoteType.GEN
      subType = CaseNoteSubType.RESET
    } else {
      type = CaseNoteType.RESET
      subType = convertCaseNotePathwayToCaseNoteSubType(pathwayType)
    }
    val creatorsList = mutableListOf<CaseNotesMeta>()
    val caseNotes = fetchCaseNotesByNomsId(type, subType, nomsId, 0)
    caseNotes.forEach { caseNote ->
      val caseNoteMeta = CaseNotesMeta(
        caseNote.authorName,
        caseNote.authorUserId,
      )
      creatorsList.add(caseNoteMeta)
    }
    return creatorsList.distinct()
  }

  fun postCaseNote(nomsId: String, pathway: Pathway, caseNotesText: String, userId: String): CaseNote? {
    val type = CaseNoteType.RESET
    val subType = convertPathwayToCaseNoteSubType(pathway)
    val prisonCode = prisonerSearchApiService.findPrisonerPersonalDetails(nomsId).prisonId

    return caseNotesClientCredentialsService.getAuthorizedClient(userId).post()
      .uri(
        "/case-notes/{nomsId}",
        nomsId,
      ).contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        mapOf(
          "locationId" to prisonCode,
          "type" to type,
          "subType" to subType,
          "text" to caseNotesText,
        ),
      )
      .retrieve()
      .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("Prisoner $nomsId not found when posting case note") })
      .bodyToMono<CaseNote>()
      .block()
  }

  fun convertCaseNotePathwayToCaseNoteSubType(caseNotePathway: CaseNotePathway) = when (caseNotePathway) {
    CaseNotePathway.All -> null
    CaseNotePathway.ACCOMMODATION -> CaseNoteSubType.ACCOM
    CaseNotePathway.ATTITUDES_THINKING_AND_BEHAVIOUR -> CaseNoteSubType.ATB
    CaseNotePathway.CHILDREN_FAMILIES_AND_COMMUNITY -> CaseNoteSubType.CHDFAMCOM
    CaseNotePathway.DRUGS_AND_ALCOHOL -> CaseNoteSubType.DRUG_ALCOHOL
    CaseNotePathway.EDUCATION_SKILLS_AND_WORK -> CaseNoteSubType.ED_SKL_WRK
    CaseNotePathway.FINANCE_AND_ID -> CaseNoteSubType.FINANCE_ID
    CaseNotePathway.HEALTH -> CaseNoteSubType.HEALTH
    CaseNotePathway.GENERAL -> CaseNoteSubType.GEN
  }

  fun convertCaseNoteSubTypeToCaseNotePathway(caseNoteSubType: CaseNoteSubType) = when (caseNoteSubType) {
    CaseNoteSubType.ACCOM -> CaseNotePathway.ACCOMMODATION
    CaseNoteSubType.ATB -> CaseNotePathway.ATTITUDES_THINKING_AND_BEHAVIOUR
    CaseNoteSubType.CHDFAMCOM -> CaseNotePathway.CHILDREN_FAMILIES_AND_COMMUNITY
    CaseNoteSubType.DRUG_ALCOHOL -> CaseNotePathway.DRUGS_AND_ALCOHOL
    CaseNoteSubType.ED_SKL_WRK -> CaseNotePathway.EDUCATION_SKILLS_AND_WORK
    CaseNoteSubType.FINANCE_ID -> CaseNotePathway.FINANCE_AND_ID
    CaseNoteSubType.HEALTH -> CaseNotePathway.HEALTH
    CaseNoteSubType.GEN, CaseNoteSubType.RESET -> CaseNotePathway.GENERAL
  }

  fun convertPathwayToCaseNoteSubType(pathway: Pathway) = when (pathway) {
    Pathway.ACCOMMODATION -> CaseNoteSubType.ACCOM
    Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR -> CaseNoteSubType.ATB
    Pathway.CHILDREN_FAMILIES_AND_COMMUNITY -> CaseNoteSubType.CHDFAMCOM
    Pathway.DRUGS_AND_ALCOHOL -> CaseNoteSubType.DRUG_ALCOHOL
    Pathway.EDUCATION_SKILLS_AND_WORK -> CaseNoteSubType.ED_SKL_WRK
    Pathway.FINANCE_AND_ID -> CaseNoteSubType.FINANCE_ID
    Pathway.HEALTH -> CaseNoteSubType.HEALTH
  }
}
