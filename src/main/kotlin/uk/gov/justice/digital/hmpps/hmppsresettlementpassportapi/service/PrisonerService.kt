package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonersList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.OffenderSearchApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonApiService
import java.util.stream.Stream

@Service
class PrisonerService(
  val offenderSearchApiService: OffenderSearchApiService,
  val prisonApiService: PrisonApiService,
  val prisonerRepository: PrisonerRepository,
  private val pathwayStatusRepository: PathwayStatusRepository,
) {
  fun getPrisonersByPrisonId(
    term: String?,
    prisonId: String,
    days: Int,
    page: Int,
    size: Int,
    sort: String,
  ): PrisonersList = offenderSearchApiService.getPrisonersByPrisonId(term, prisonId, days, page, size, sort)

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPrisonerDetailsByNomsId(nomsId: String) = offenderSearchApiService.getPrisonerDetailsByNomsId(nomsId)

  fun getPrisonerImageData(nomsId: String, imageId: Int): ByteArray? = prisonApiService.getPrisonerImageData(nomsId, imageId)

  @Transactional
  fun getInProgressNomsIdsByPrisonId(prisonId: String): List<String> {
    val inProgressPrisoners = mutableListOf<String>()
    val inProgressOrDonePrisoners = pathwayStatusRepository.findPrisonersByPrisonIdWithAtLeastOnePathwayNotInNotStarted(prisonId)
    val donePrisoners = pathwayStatusRepository.findPrisonersByPrisonWithAllPathwaysDone(prisonId)
    inProgressPrisoners.addAll(inProgressOrDonePrisoners)
    inProgressPrisoners.removeAll(donePrisoners)
    return inProgressPrisoners
  }

  @Transactional
  fun getNotStartedNomsIdsByPrisonId(prisonId: String) = pathwayStatusRepository.findPrisonersByPrisonIdWithAllPathwaysNotStarted(prisonId)

  @Transactional
  fun getDoneNomsIdsByPrisonId(prisonId: String) = pathwayStatusRepository.findPrisonersByPrisonWithAllPathwaysDone(prisonId)

  @Transactional
  fun addReleaseDateToPrisoners() {
    log.info("Start updating prisoner entities to add release date")

    val prisonersToUpdate = prisonerRepository.findAllByReleaseDateIsNull()

    log.info("Found [${prisonersToUpdate.size}] prisoners to update")

    prisonersToUpdate.forEach { prisonerEntity ->
      val prisoner = offenderSearchApiService.findPrisonerPersonalDetails(prisonerEntity.nomsId)
      offenderSearchApiService.setDisplayedReleaseDate(prisoner)
      prisonerEntity.releaseDate = prisoner.confirmedReleaseDate
    }

    prisonerRepository.saveAll(prisonersToUpdate)
    log.info("Finished updating prisoner entities to add release date")
  }

  fun getSliceOfAllPrisoners(page: Pageable): Slice<PrisonerEntity> = prisonerRepository.findAll(page)

  @Transactional
  fun updateAndSaveNewReleaseDates(prisonerEntities: Stream<PrisonerEntity>) {
    for (prisonerEntity in prisonerEntities) {
      try {
        val prisoner = offenderSearchApiService.findPrisonerPersonalDetails(prisonerEntity.nomsId)
        offenderSearchApiService.setDisplayedReleaseDate(prisoner)
        prisonerEntity.releaseDate = prisoner.confirmedReleaseDate
        prisonerRepository.save(prisonerEntity)
      } catch (e: ResourceNotFoundException) {
        log.warn("Cannot update release date for prisoner ${prisonerEntity.nomsId} as no results from Prisoner Search API - skipping until next cron run.")
      }
    }
  }
}
