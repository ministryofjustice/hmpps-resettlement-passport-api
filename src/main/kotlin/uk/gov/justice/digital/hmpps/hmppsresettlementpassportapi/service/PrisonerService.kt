package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonersList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.OffenderSearchApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonApiService
import java.time.LocalDate
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
    pathwayView: Pathway?,
    pathwayStatus: Status?,
    page: Int,
    size: Int,
    sort: String,
  ): PrisonersList {
    if (pathwayStatus != null && pathwayView == null) {
      throw ServerWebInputException("pathwayStatus cannot be used without pathwayView")
    }
    if (pathwayView == null && (sort == "pathwayStatus,ASC" || sort == "pathwayStatus,DESC")) {
      throw ServerWebInputException("Pathway must be selected to sort by pathway status")
    }
    return offenderSearchApiService.getPrisonersByPrisonId(term, prisonId, days, pathwayView, pathwayStatus, page, size, sort)
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPrisonerDetailsByNomsId(nomsId: String) = offenderSearchApiService.getPrisonerDetailsByNomsId(nomsId)

  fun getPrisonerImageData(nomsId: String, imageId: Int): ByteArray? = prisonApiService.getPrisonerImageData(nomsId, imageId)

  @Transactional
  fun getInProgressPrisonersByPrisonId(prisonId: String, earliestReleaseDate: LocalDate, latestReleaseDate: LocalDate): List<PrisonerEntity> {
    val inProgressPrisoners = mutableListOf<PrisonerEntity>()
    val inProgressOrDonePrisoners = pathwayStatusRepository.findPrisonersByPrisonIdWithAtLeastOnePathwayNotInNotStarted(prisonId, earliestReleaseDate, latestReleaseDate)
    val donePrisoners = pathwayStatusRepository.findPrisonersByPrisonWithAllPathwaysDone(prisonId, earliestReleaseDate, latestReleaseDate)
    inProgressPrisoners.addAll(inProgressOrDonePrisoners)
    inProgressPrisoners.removeAll(donePrisoners)
    return inProgressPrisoners
  }

  @Transactional
  fun getNotStartedPrisonersByPrisonId(prisonId: String, earliestReleaseDate: LocalDate, latestReleaseDate: LocalDate) = pathwayStatusRepository.findPrisonersByPrisonIdWithAllPathwaysNotStarted(prisonId, earliestReleaseDate, latestReleaseDate)

  @Transactional
  fun getDonePrisonersByPrisonId(prisonId: String, earliestReleaseDate: LocalDate, latestReleaseDate: LocalDate) = pathwayStatusRepository.findPrisonersByPrisonWithAllPathwaysDone(prisonId, earliestReleaseDate, latestReleaseDate)

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
