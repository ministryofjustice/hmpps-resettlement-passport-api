package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesMeta
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.CaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.OffenderCaseNotesApiService

@Service
class CaseNotesService(val offenderCaseNotesApiService: OffenderCaseNotesApiService) {
  fun getCaseNotesByNomsId(nomsId: String, page: Int, size: Int, sort: String, days: Int, pathwayType: String, createdByUserId: Int): CaseNotesList {
    return offenderCaseNotesApiService.getCaseNotesByNomsId(nomsId, page, size, sort, days, pathwayType, createdByUserId)
  }

  fun getCaseNotesCreatorsByPathway(nomsId: String, pathwayType: String): List<CaseNotesMeta> {
    return offenderCaseNotesApiService.getCaseNotesCreatorsByPathway(nomsId, pathwayType)
  }

  fun postCaseNote(nomsId: String, caseNotes: CaseNotesRequest, auth: String): CaseNote? {
    return offenderCaseNotesApiService.postCaseNote(nomsId, caseNotes, auth)
  }
}
