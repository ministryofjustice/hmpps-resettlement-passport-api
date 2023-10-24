package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonersList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.OffenderSearchApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonApiService

@Service
class PrisonerService(val offenderSearchApiService: OffenderSearchApiService, val prisonApiService: PrisonApiService) {
  suspend fun getPrisonersByPrisonId(
    term: String,
    prisonId: String,
    days: Int,
    page: Int,
    size: Int,
    sort: String,
  ): PrisonersList = offenderSearchApiService.getPrisonersByPrisonId(term, prisonId, days, page, size, sort)

  suspend fun getPrisonerDetailsByNomsId(nomsId: String) = offenderSearchApiService.getPrisonerDetailsByNomsId(nomsId)

  suspend fun getPrisonerImageData(nomsId: String, imageId: Int) = prisonApiService.getPrisonerImageData(nomsId, imageId)

  suspend fun updatePrisonIdInPrisoners() = offenderSearchApiService.updatePrisonId()
}
