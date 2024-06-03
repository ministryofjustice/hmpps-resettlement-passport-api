package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external

import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientException
import org.springframework.web.reactive.function.client.bodyToMono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotePathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNoteType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesMeta
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DpsCaseNoteSubType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DpsCaseNoteType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.CaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.CaseNotes
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.CaseNotesClientCredentialsService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.enumIncludes
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.exponentialBackOffRetry
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.extractCaseNoteTypeFromBcstCaseNote
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
    caseNoteType: CaseNoteType,
    createdBy: Int,
  ): List<PathwayCaseNote> {
    val caseNotes = mutableListOf<CaseNote>()
    if (caseNoteType == CaseNoteType.All) {
      caseNotes.addAll(fetchCaseNotesByNomsId(DpsCaseNoteType.GEN, DpsCaseNoteSubType.RESET, nomsId, days))
      val caseNotesRESET = fetchCaseNotesByNomsId(DpsCaseNoteType.RESET, null, nomsId, days)
      caseNotesRESET.forEach {
        val currentCaseNoteType = extractCaseNoteTypeFromBcstCaseNote(it.text)
        if (currentCaseNoteType != null) {
          it.subType = convertCaseNoteTypeToCaseNoteSubType(currentCaseNoteType)?.name!!
        }
        caseNotes.add(it)
      }
      caseNotes.addAll(caseNotesRESET)
    } else {
      val subType = convertCaseNoteTypeToCaseNoteSubType(caseNoteType)
      caseNotes.addAll(fetchCaseNotesByNomsId(DpsCaseNoteType.RESET, subType, nomsId, days))
      // We also need to get any BCST case notes where the first line indicates it's related to a pathway
      val caseNotesBcst = fetchCaseNotesByNomsId(DpsCaseNoteType.RESET, DpsCaseNoteSubType.BCST, nomsId, days)
      caseNotesBcst.forEach {
        val currentCaseNoteType = extractCaseNoteTypeFromBcstCaseNote(it.text)
        if (currentCaseNoteType == caseNoteType) {
          it.subType = convertCaseNoteTypeToCaseNoteSubType(currentCaseNoteType)?.name!!
          caseNotes.add(it)
        }
      }
    }

    if (createdBy != 0) {
      caseNotes.retainAll { it.authorUserId.toInt() == createdBy }
    }

    return mapCaseNotes(caseNotes)
  }

  private fun fetchCaseNotesByNomsId(
    type: DpsCaseNoteType,
    subType: DpsCaseNoteSubType?,
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
      val pathwayType = if (enumIncludes<DpsCaseNoteSubType>(caseNote.subType)) {
        convertCaseNoteSubTypeToCaseNotePathway(DpsCaseNoteSubType.valueOf(caseNote.subType))
      } else {
        CaseNotePathway.OTHER
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

  fun getCaseNotesCreatorsByPathway(nomsId: String, caseNoteType: CaseNoteType): List<CaseNotesMeta> {
    val type = DpsCaseNoteType.RESET
    val subType = convertCaseNoteTypeToCaseNoteSubType(caseNoteType)
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

  fun postCaseNote(nomsId: String, caseNotesText: String, userId: String, subType: DpsCaseNoteSubType): CaseNote? {
    val type = DpsCaseNoteType.RESET
    val prisonCode = prisonerSearchApiService.findPrisonerPersonalDetails(nomsId).prisonId

    return runBlocking {
      caseNotesClientCredentialsService.getAuthorizedClient(userId).post()
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
        .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("Prisoner $nomsId not found when posting case note of type $type and subtype $subType") })
        .bodyToMono<CaseNote>()
        .exponentialBackOffRetry()
        .awaitSingle()
    }
  }

  fun convertCaseNoteTypeToCaseNoteSubType(caseNoteType: CaseNoteType) = when (caseNoteType) {
    CaseNoteType.All -> null
    CaseNoteType.ACCOMMODATION -> DpsCaseNoteSubType.ACCOM
    CaseNoteType.ATTITUDES_THINKING_AND_BEHAVIOUR -> DpsCaseNoteSubType.ATB
    CaseNoteType.CHILDREN_FAMILIES_AND_COMMUNITY -> DpsCaseNoteSubType.CHDFAMCOM
    CaseNoteType.DRUGS_AND_ALCOHOL -> DpsCaseNoteSubType.DRUG_ALCOHOL
    CaseNoteType.EDUCATION_SKILLS_AND_WORK -> DpsCaseNoteSubType.ED_SKL_WRK
    CaseNoteType.FINANCE_AND_ID -> DpsCaseNoteSubType.FINANCE_ID
    CaseNoteType.HEALTH -> DpsCaseNoteSubType.HEALTH
  }

  fun convertCaseNoteSubTypeToCaseNotePathway(dpsCaseNoteSubType: DpsCaseNoteSubType) = when (dpsCaseNoteSubType) {
    DpsCaseNoteSubType.ACCOM -> CaseNotePathway.ACCOMMODATION
    DpsCaseNoteSubType.ATB -> CaseNotePathway.ATTITUDES_THINKING_AND_BEHAVIOUR
    DpsCaseNoteSubType.CHDFAMCOM -> CaseNotePathway.CHILDREN_FAMILIES_AND_COMMUNITY
    DpsCaseNoteSubType.DRUG_ALCOHOL -> CaseNotePathway.DRUGS_AND_ALCOHOL
    DpsCaseNoteSubType.ED_SKL_WRK -> CaseNotePathway.EDUCATION_SKILLS_AND_WORK
    DpsCaseNoteSubType.FINANCE_ID -> CaseNotePathway.FINANCE_AND_ID
    DpsCaseNoteSubType.HEALTH -> CaseNotePathway.HEALTH
    DpsCaseNoteSubType.GEN, DpsCaseNoteSubType.RESET, DpsCaseNoteSubType.BCST, DpsCaseNoteSubType.INR, DpsCaseNoteSubType.PRR -> CaseNotePathway.OTHER
  }

  fun convertPathwayToCaseNoteSubType(pathway: Pathway) = when (pathway) {
    Pathway.ACCOMMODATION -> DpsCaseNoteSubType.ACCOM
    Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR -> DpsCaseNoteSubType.ATB
    Pathway.CHILDREN_FAMILIES_AND_COMMUNITY -> DpsCaseNoteSubType.CHDFAMCOM
    Pathway.DRUGS_AND_ALCOHOL -> DpsCaseNoteSubType.DRUG_ALCOHOL
    Pathway.EDUCATION_SKILLS_AND_WORK -> DpsCaseNoteSubType.ED_SKL_WRK
    Pathway.FINANCE_AND_ID -> DpsCaseNoteSubType.FINANCE_ID
    Pathway.HEALTH -> DpsCaseNoteSubType.HEALTH
  }
}
