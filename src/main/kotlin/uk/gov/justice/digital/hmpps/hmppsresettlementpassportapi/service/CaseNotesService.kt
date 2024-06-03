package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNoteType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesMeta
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DeliusCaseNoteType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DpsCaseNoteSubType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.DeliusAuthor
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.CaseNotesApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import java.time.OffsetDateTime
import java.util.Objects

@Service
class CaseNotesService(
  val caseNotesApiService: CaseNotesApiService,
  val deliusContactService: DeliusContactService,
  val objectMapper: ObjectMapper,
  val prisonerRepository: PrisonerRepository,
  val resettlementPassportDeliusApiService: ResettlementPassportDeliusApiService,
) {
  fun getCaseNotesByNomsId(nomsId: String, page: Int, size: Int, sort: String, days: Int, caseNoteType: CaseNoteType, createdByUserId: Int): CaseNotesList {
    if (page < 0 || size <= 0) {
      throw NoDataWithCodeFoundException(
        "Data",
        "Page $page and Size $size",
      )
    }

    if (nomsId.isBlank()) {
      throw NoDataWithCodeFoundException("Prisoner", nomsId)
    }

    var combinedCaseNotes = mutableListOf<PathwayCaseNote>()

    // Get case notes from DPS Case Notes API, delius_contact table and dps_case_notes
    combinedCaseNotes.addAll(caseNotesApiService.getCaseNotesByNomsId(nomsId, days, caseNoteType, createdByUserId))
    if (createdByUserId == 0) { // RP2-900 For now for can't filter non-DPS case notes by the user. In this case just don't show anything from the database/Delius
      combinedCaseNotes.addAll(deliusContactService.getCaseNotesByNomsId(nomsId, caseNoteType))
    }

    // Remove duplicates
    combinedCaseNotes = removeDuplicates(combinedCaseNotes)

    val sortValue = sort.ifBlank {
      "occurrenceDateTime,DESC"
    }

    when (sortValue) {
      // TODO remove spelling mistakes once UI is updated.
      "occurenceDateTime,ASC", "occurrenceDateTime,ASC" -> combinedCaseNotes.sortBy { it.occurenceDateTime }
      "pathway,ASC" -> combinedCaseNotes.sortBy { it.pathway }
      "occurenceDateTime,DESC", "occurrenceDateTime,DESC" -> combinedCaseNotes.sortByDescending { it.occurenceDateTime }
      "pathway,DESC" -> combinedCaseNotes.sortByDescending { it.pathway }
      else -> throw NoDataWithCodeFoundException(
        "Data",
        "Sort value Invalid",
      )
    }

    val startIndex = page * size
    if (combinedCaseNotes.size != 0 && startIndex >= combinedCaseNotes.size) {
      throw NoDataWithCodeFoundException(
        "Data",
        "Page $page",
      )
    }

    val endIndex = startIndex + size
    if (startIndex < endIndex && endIndex <= combinedCaseNotes.size) {
      val caseNotesPageList = combinedCaseNotes.subList(startIndex, endIndex)
      return CaseNotesList(
        caseNotesPageList,
        caseNotesPageList.size,
        page,
        sort,
        combinedCaseNotes.size,
        endIndex == combinedCaseNotes.size,
      )
    } else if (startIndex < endIndex) {
      val caseNotesPageList = combinedCaseNotes.subList(startIndex, combinedCaseNotes.size)
      return CaseNotesList(caseNotesPageList, caseNotesPageList.size, page, sort, combinedCaseNotes.size, true)
    }

    return CaseNotesList(null, null, null, null, 0, false)
  }

  fun getCaseNotesCreatorsByPathway(nomsId: String, caseNoteType: CaseNoteType): List<CaseNotesMeta> {
    return caseNotesApiService.getCaseNotesCreatorsByPathway(nomsId, caseNoteType)
  }

  // Remove duplicates based on the createdBy + text + creationDate + occurrenceDate + pathway
  fun removeDuplicates(caseNotes: List<PathwayCaseNote>) = caseNotes.distinctBy { Objects.hash(it.createdBy, it.text, it.creationDateTime.toLocalDate(), it.occurenceDateTime.toLocalDate(), it.pathway) }.toMutableList()

  fun postBCSTCaseNoteToDps(
    nomsId: String,
    notes: String,
    userId: String,
    subType: DpsCaseNoteSubType,
  ) {
    caseNotesApiService.postCaseNote(
      nomsId = nomsId,
      caseNotesText = notes,
      userId = userId,
      subType = subType,
    )
  }

  fun postBCSTCaseNoteToDelius(crn: String, prisonCode: String, notes: String, name: String, assessmentType: ResettlementAssessmentType) {
    val type = when(assessmentType) {
      ResettlementAssessmentType.BCST2 -> DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT
      ResettlementAssessmentType.RESETTLEMENT_PLAN -> DeliusCaseNoteType.PRE_RELEASE_REPORT
    }

    resettlementPassportDeliusApiService.createContact(
      crn = crn,
      type = type,
      dateTime = OffsetDateTime.now(),
      notes = notes,
      author = convertFromNameToDeliusAuthor(prisonCode, name),
    )
  }
}
