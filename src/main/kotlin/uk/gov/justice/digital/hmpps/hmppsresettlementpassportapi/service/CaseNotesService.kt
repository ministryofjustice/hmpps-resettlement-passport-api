package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotePathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesMeta
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.OffenderCaseNotesApiService

@Service
class CaseNotesService(val offenderCaseNotesApiService: OffenderCaseNotesApiService, val deliusContactService: DeliusContactService) {
  fun getCaseNotesByNomsId(nomsId: String, page: Int, size: Int, sort: String, days: Int, pathwayType: CaseNotePathway, createdByUserId: Int): CaseNotesList {
    if (page < 0 || size <= 0) {
      throw NoDataWithCodeFoundException(
        "Data",
        "Page $page and Size $size",
      )
    }

    if (nomsId.isBlank()) {
      throw NoDataWithCodeFoundException("Prisoner", nomsId)
    }

    val combinedCaseNotes = mutableListOf<PathwayCaseNote>()

    combinedCaseNotes.addAll(offenderCaseNotesApiService.getCaseNotesByNomsId(nomsId, days, pathwayType, createdByUserId))
    if (createdByUserId == 0) { // RP2-900 For now for can't filter non-NOMIS case notes by the user. In this case just don't show anything from the database/Delius
      combinedCaseNotes.addAll(deliusContactService.getCaseNotesByNomsId(nomsId, pathwayType))
    }

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

  fun getCaseNotesCreatorsByPathway(nomsId: String, pathwayType: CaseNotePathway): List<CaseNotesMeta> {
    return offenderCaseNotesApiService.getCaseNotesCreatorsByPathway(nomsId, pathwayType)
  }
}
