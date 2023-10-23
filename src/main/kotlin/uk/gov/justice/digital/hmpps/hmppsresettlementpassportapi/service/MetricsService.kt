package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.OffenderSearchApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonRegisterApiService
import java.time.LocalDate

@Service
class MetricsService(
  private val offenderSearchApiService: OffenderSearchApiService,
  private val prisonRegisterApiService: PrisonRegisterApiService,
  private val prisonerRepository: PrisonerRepository,
  private val pathwayStatusRepository: PathwayStatusRepository,
  private val registry: MeterRegistry,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun recordPrisonersCountForEachPrison() {
    val prisonList = prisonRegisterApiService.getActivePrisonsList()
    val offenders = mutableListOf<PrisonersSearch>()
    val offenders12Weeks = mutableListOf<PrisonersSearch>()
    val offenders24Weeks = mutableListOf<PrisonersSearch>()
    val offendersAllTime = mutableListOf<PrisonersSearch>()
    for (item in prisonList) {
      try {
        if (item.active) {
          offenderSearchApiService.findPrisonersBySearchTerm(item.id, "").collect {
            it.forEach {
              if (it.confirmedReleaseDate != null) {
                it.displayReleaseDate = it.confirmedReleaseDate
              } else if (it.actualParoleDate != null) {
                it.displayReleaseDate = it.actualParoleDate
              } else if (it.homeDetentionCurfewActualDate != null) {
                it.displayReleaseDate = it.homeDetentionCurfewActualDate
              } else if (it.conditionalReleaseDate != null) {
                it.displayReleaseDate = it.conditionalReleaseDate
              } else if (it.automaticReleaseDate != null) {
                it.displayReleaseDate = it.automaticReleaseDate
              } else {
                it.displayReleaseDate = null
              }
              offenders.add(it)
              offenders12Weeks.add(it)
              offenders24Weeks.add(it)
              offendersAllTime.add(it)
            }
            registry.gauge("total_prisoners_count", Tags.of("prison", item.name), offenders.size)

            val earliestReleaseDate = LocalDate.now().minusDays(1)
            var latestReleaseDate = LocalDate.now().plusDays(84)
            offenders12Weeks.removeAll { it.displayReleaseDate == null || it.displayReleaseDate!! <= earliestReleaseDate || it.displayReleaseDate!! > latestReleaseDate }
            registry.gauge("total_prisoners_12Weeks_count", Tags.of("prison", item.name), offenders12Weeks.size)
            latestReleaseDate = LocalDate.now().plusDays(168)
            offenders24Weeks.removeAll { it.displayReleaseDate == null || it.displayReleaseDate!! <= earliestReleaseDate || it.displayReleaseDate!! > latestReleaseDate }
            registry.gauge("total_prisoners_24Weeks_count", Tags.of("prison", item.name), offenders24Weeks.size)

            offendersAllTime.removeAll { it.displayReleaseDate == null || it.displayReleaseDate!! >= earliestReleaseDate }
            registry.gauge("total_prisoners_AllTime_count", Tags.of("prison", item.name), offendersAllTime.size)
          }
        }
      } catch (ex: Exception) {
        log.warn("Prisoners data not found. Unable to fetch Prisoners for the Prison Id ${item.id}")
      }
    }
  }
}
