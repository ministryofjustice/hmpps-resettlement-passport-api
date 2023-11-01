package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonersList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.OffenderSearchApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonApiService

@Service
class PrisonerService(
  val offenderSearchApiService: OffenderSearchApiService,
  val prisonApiService: PrisonApiService,
  val prisonerRepository: PrisonerRepository,
  private val pathwayStatusRepository: PathwayStatusRepository,
) {
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

  @Transactional
  suspend fun getInProgressNomsIdsByPrisonId(prisonId: String): List<String> {
    val inProgressPrisoners = mutableListOf<String>()
    val inProgressOrDonePrisoners = pathwayStatusRepository.findPrisonersByPrisonIdWithAtLeastOnePathwayNotInNotStarted(prisonId)
    val donePrisoners = pathwayStatusRepository.findPrisonersByPrisonWithAllPathwaysDone(prisonId)
    inProgressPrisoners.addAll(inProgressOrDonePrisoners)
    inProgressPrisoners.removeAll(donePrisoners)
    return inProgressPrisoners
  }

  @Transactional
  suspend fun getNotStartedNomsIdsByPrisonId(prisonId: String) = pathwayStatusRepository.findPrisonersByPrisonIdWithAllPathwaysNotStarted(prisonId)

  @Transactional
  suspend fun getDoneNomsIdsByPrisonId(prisonId: String) = pathwayStatusRepository.findPrisonersByPrisonWithAllPathwaysDone(prisonId)
}
