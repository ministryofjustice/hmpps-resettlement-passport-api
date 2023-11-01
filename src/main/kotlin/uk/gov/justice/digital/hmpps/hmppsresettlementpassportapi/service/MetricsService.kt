package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.OffenderSearchApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonRegisterApiService
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicLong

@Service
class MetricsService(
  private val offenderSearchApiService: OffenderSearchApiService,
  private val prisonRegisterApiService: PrisonRegisterApiService,
  private val prisonerService: PrisonerService,
  private val registry: MeterRegistry,
  ) {

  private val prisonersCountMap = HashMap<String, Int>()

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun recordCustomMetrics() {
    recordPrisonersCountForEachPrison()
  }

  private suspend fun recordPrisonersCountForEachPrison() {
    val prisonList = prisonRegisterApiService.getActivePrisonsList()
    val earliestReleaseDate = LocalDate.now().minusDays(1)
    val latestRD12Weeks = LocalDate.now().plusDays(84)
    val latestRD24Weeks = LocalDate.now().plusDays(168)
    for (prison in prisonList) {
      var totalPrisonersCount = 0
      var totalPrisoners12WeeksCount = 0
      var totalPrisoners24WeeksCount = 0
      var notStartedPrisonersCount = 0
      var notStartedPrisoners12WeeksCount = 0
      var notStartedPrisoners24WeeksCount = 0
      var inProgressPrisonersCount = 0
      var inProgressPrisoners12WeeksCount = 0
      var inProgressPrisoners24WeeksCount = 0
      var donePrisonersCount = 0
      var donePrisoners12WeeksCount = 0
      var donePrisoners24WeeksCount = 0

      try {
        val activePrisonersList = prisonerService.getNotStartedNomsIdsByPrisonId(prison.id)
        val inProgressPrisonersList = prisonerService.getInProgressNomsIdsByPrisonId(prison.id)
        val donePrisonersList = prisonerService.getDoneNomsIdsByPrisonId(prison.id)

        offenderSearchApiService.findPrisonersBySearchTerm(prison.id, "").collect { prisoners ->
          prisoners.forEach { prisoner ->
            totalPrisonersCount++

            offenderSearchApiService.setDisplayedReleaseDate(prisoner)

            if (activePrisonersList.contains(prisoner.prisonerNumber)) {
              notStartedPrisonersCount++
            }
            if (inProgressPrisonersList.contains(prisoner.prisonerNumber)) {
              inProgressPrisonersCount++
            }
            if (donePrisonersList.contains(prisoner.prisonerNumber)) {
              donePrisonersCount++
            }
            if (prisoner.displayReleaseDate != null && (prisoner.displayReleaseDate!! > earliestReleaseDate && prisoner.displayReleaseDate!! <= latestRD12Weeks)) {
              totalPrisoners12WeeksCount++
              if (activePrisonersList.contains(prisoner.prisonerNumber)) {
                notStartedPrisoners12WeeksCount++
              }
              if (inProgressPrisonersList.contains(prisoner.prisonerNumber)) {
                inProgressPrisoners12WeeksCount++
              }
              if (donePrisonersList.contains(prisoner.prisonerNumber)) {
                donePrisoners12WeeksCount++
              }
            }

            if (prisoner.displayReleaseDate != null && (prisoner.displayReleaseDate!! > earliestReleaseDate && prisoner.displayReleaseDate!! <= latestRD24Weeks)) {
              totalPrisoners24WeeksCount++
              if (activePrisonersList.contains(prisoner.prisonerNumber)) {
                notStartedPrisoners24WeeksCount++
              }
              if (inProgressPrisonersList.contains(prisoner.prisonerNumber)) {
                inProgressPrisoners24WeeksCount++
              }
              if (donePrisonersList.contains(prisoner.prisonerNumber)) {
                donePrisoners24WeeksCount++
              }
            }
          }
        }

        prisonersCountMap["total_in_progress_prisoners_count_${prison.id}"] = inProgressPrisonersCount
        prisonersCountMap["total_in_progress_prisoners_12Weeks_count_${prison.id}"] = inProgressPrisoners12WeeksCount
        prisonersCountMap["total_in_progress_prisoners_24Weeks_count_${prison.id}"] = inProgressPrisoners24WeeksCount

        prisonersCountMap["total_not_started_prisoners_count_${prison.id}"] = notStartedPrisonersCount
        prisonersCountMap["total_not_started_prisoners_12Weeks_count_${prison.id}"] = notStartedPrisoners12WeeksCount
        prisonersCountMap["total_not_started_prisoners_24Weeks_count_${prison.id}"] = notStartedPrisoners24WeeksCount

        prisonersCountMap["total_prisoners_count_${prison.id}"] = totalPrisonersCount
        prisonersCountMap["total_prisoners_12Weeks_count_${prison.id}"] = totalPrisoners12WeeksCount
        prisonersCountMap["total_prisoners_24Weeks_count_${prison.id}"] = totalPrisoners24WeeksCount

        prisonersCountMap["total_done_prisoners_count_${prison.id}"] = donePrisonersCount
        prisonersCountMap["total_done_prisoners_12Weeks_count_${prison.id}"] = donePrisoners12WeeksCount
        prisonersCountMap["total_done_prisoners_24Weeks_count_${prison.id}"] = donePrisoners24WeeksCount

        val prisonTag = Tags.of("prison", prison.name)

        registry.gauge(
          "total_prisoners_count",
          prisonTag.and("releaseDate", "Overall"),
          prisonersCountMap,
        ) {
          it.getValueFromMapAsDouble("total_prisoners_count_${prison.id}")
        }

        registry.gauge(
          "total_prisoners_count",
          prisonTag.and("releaseDate", "12 Weeks"),
          prisonersCountMap,
        ) {
          it.getValueFromMapAsDouble("total_prisoners_12Weeks_count_${prison.id}")
        }

        registry.gauge(
          "total_prisoners_count",
          prisonTag.and("releaseDate", "24 Weeks"),
          prisonersCountMap,
        ) {
          it.getValueFromMapAsDouble("total_prisoners_24Weeks_count_${prison.id}")
        }

        registry.gauge(
          "total_not_started_prisoners_count",
          prisonTag.and("releaseDate", "Overall"),
          prisonersCountMap,
        ) {
          it.getValueFromMapAsDouble("total_not_started_prisoners_count_${prison.id}")
        }

        registry.gauge(
          "total_not_started_prisoners_count",
          prisonTag.and("releaseDate", "12 Weeks"),
          prisonersCountMap,
        ) {
          it.getValueFromMapAsDouble("total_not_started_prisoners_12Weeks_count_${prison.id}")
        }

        registry.gauge(
          "total_not_started_prisoners_count",
          prisonTag.and("releaseDate", "24 Weeks"),
          prisonersCountMap,
        ) {
          it.getValueFromMapAsDouble("total_not_started_prisoners_24Weeks_count_${prison.id}")
        }

        registry.gauge(
          "total_in_progress_prisoners_count",
          prisonTag.and("releaseDate", "Overall"),
          prisonersCountMap,
        ) {
          it.getValueFromMapAsDouble("total_in_progress_prisoners_count_${prison.id}")
        }

        registry.gauge(
          "total_in_progress_prisoners_count",
          prisonTag.and("releaseDate", "12 Weeks"),
          prisonersCountMap,
        ) {
          it.getValueFromMapAsDouble("total_in_progress_prisoners_12Weeks_count_${prison.id}")
        }

        registry.gauge(
          "total_in_progress_prisoners_count",
          prisonTag.and("releaseDate", "24 Weeks"),
          prisonersCountMap,
        ) {
          it.getValueFromMapAsDouble("total_in_progress_prisoners_24Weeks_count_${prison.id}")
        }

        registry.gauge(
          "total_done_prisoners_count",
          prisonTag.and("releaseDate", "Overall"),
          prisonersCountMap,
        ) {
          it.getValueFromMapAsDouble("total_done_prisoners_count_${prison.id}")
        }

        registry.gauge(
          "total_done_prisoners_count",
          prisonTag.and("releaseDate", "12 Weeks"),
          prisonersCountMap,
        ) {
          it.getValueFromMapAsDouble("total_done_prisoners_12Weeks_count_${prison.id}")
        }

        registry.gauge(
          "total_done_prisoners_count",
          prisonTag.and("releaseDate", "24 Weeks"),
          prisonersCountMap,
        ) {
          it.getValueFromMapAsDouble("total_done_prisoners_24Weeks_count_${prison.id}")
        }

      } catch (ex: Exception) {
        log.warn("Prisoners data not found. Unable to fetch Prisoners for the Prison Id ${prison.id}")
      }
    }
  }
}
