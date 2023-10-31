package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonersList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
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

  suspend fun getActivePrisonersCountByPrisonId(prisonId: String) = prisonerRepository.countByPrisonId(prisonId)

  @Transactional
  suspend fun getActiveNomisIdsByPrisonId(prisonId: String) = prisonerRepository.findNomisIdsByPrisonId(prisonId)

  @Transactional
  suspend fun getInUseNomisIdsByPrisonId(prisonId: String) = pathwayStatusRepository.findInUsePrisonersByPrisonIdAndStatus(prisonId, Status.NOT_STARTED.id)

  @Transactional
  suspend fun getNotStartedNomisIdsByPrisonId(prisonId: String) = pathwayStatusRepository.findNotStartedPrisonersByPrisonIdAndStatus(prisonId, Status.NOT_STARTED.id)
}
