package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesMeta
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi.CaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.OffenderCaseNotesApiService

@Service
class CaseNotesService(val offenderCaseNotesApiService: OffenderCaseNotesApiService) {
  suspend fun getCaseNotesByNomsId(nomsId: String, page: Int, size: Int, sort: String, days: Int, pathwayType: String, createdByUserId: Int): CaseNotesList {
    return offenderCaseNotesApiService.getCaseNotesByNomsId(nomsId, page, size, sort, days, pathwayType, createdByUserId)
  }

  suspend fun getCaseNotesCreatorsByPathway(nomsId: String, pathwayType: String): List<CaseNotesMeta> {
    return offenderCaseNotesApiService.getCaseNotesCreatorsByPathway(nomsId, pathwayType)
  }

  suspend fun postCaseNote(prisonerId: String, casenotes: CaseNotesRequest, auth: String): CaseNote {
    return offenderCaseNotesApiService.postCaseNote(prisonerId, casenotes, auth)
  }
}