package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.OffenderSearchApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonRegisterApiService
import java.time.LocalDate

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

  fun recordCustomMetrics() {
    recordPrisonersCountForEachPrison()
  }

  private fun recordPrisonersCountForEachPrison() {

    log.info("Started running scheduled metrics job")

    val earliestReleaseDate = LocalDate.now().minusDays(1)
    val latestAllTimeReleaseDate = LocalDate.parse("9999-12-31")
    val latestRD12Weeks = LocalDate.now().plusDays(84)
    val latestRD24Weeks = LocalDate.now().plusDays(168)

    val prisonList = prisonRegisterApiService.getActivePrisonsList()

    for (prison in prisonList) {
      try {

        offenderSearchApiService.findPrisonersBySearchTerm(prison.id, "").collect { prisoners ->
          prisoners.forEach { offenderSearchApiService.setDisplayedReleaseDate(it) }
          prisonersCountMap["total_prisoners_count_${prison.id}"] = prisoners.filter { it.displayReleaseDate == null || it.displayReleaseDate!! > earliestReleaseDate }.size
          prisonersCountMap["total_prisoners_12Weeks_count_${prison.id}"] = prisoners.filter { it.displayReleaseDate != null && it.displayReleaseDate!! > earliestReleaseDate && it.displayReleaseDate!! < latestRD12Weeks }.size
          prisonersCountMap["total_prisoners_24Weeks_count_${prison.id}"] = prisoners.filter { it.displayReleaseDate != null && it.displayReleaseDate!! > earliestReleaseDate && it.displayReleaseDate!! < latestRD24Weeks }.size
        }

        prisonersCountMap["total_not_started_prisoners_count_${prison.id}"] = prisonerService.getNotStartedPrisonersByPrisonId(prison.id, earliestReleaseDate, latestAllTimeReleaseDate).size
        prisonersCountMap["total_not_started_prisoners_12Weeks_count_${prison.id}"] = prisonerService.getNotStartedPrisonersByPrisonId(prison.id, earliestReleaseDate, latestRD12Weeks).filter { it.releaseDate != null }.size
        prisonersCountMap["total_not_started_prisoners_24Weeks_count_${prison.id}"] = prisonerService.getNotStartedPrisonersByPrisonId(prison.id, earliestReleaseDate, latestRD24Weeks).filter { it.releaseDate != null }.size

        prisonersCountMap["total_in_progress_prisoners_count_${prison.id}"] = prisonerService.getInProgressPrisonersByPrisonId(prison.id, earliestReleaseDate, latestAllTimeReleaseDate).size
        prisonersCountMap["total_in_progress_prisoners_12Weeks_count_${prison.id}"] = prisonerService.getInProgressPrisonersByPrisonId(prison.id, earliestReleaseDate, latestRD12Weeks).filter { it.releaseDate != null }.size
        prisonersCountMap["total_in_progress_prisoners_24Weeks_count_${prison.id}"] = prisonerService.getInProgressPrisonersByPrisonId(prison.id, earliestReleaseDate, latestRD24Weeks).filter { it.releaseDate != null }.size

        prisonersCountMap["total_done_prisoners_count_${prison.id}"] = prisonerService.getDonePrisonersByPrisonId(prison.id, earliestReleaseDate, latestAllTimeReleaseDate).size
        prisonersCountMap["total_done_prisoners_12Weeks_count_${prison.id}"] = prisonerService.getDonePrisonersByPrisonId(prison.id, earliestReleaseDate, latestRD12Weeks).filter { it.releaseDate != null }.size
        prisonersCountMap["total_done_prisoners_24Weeks_count_${prison.id}"] = prisonerService.getDonePrisonersByPrisonId(prison.id, earliestReleaseDate, latestRD24Weeks).filter { it.releaseDate != null }.size

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
      } catch (_: ResourceNotFoundException) {
      } catch (ex: Exception) {
        log.warn("Error collecting metrics for prison ${prison.name}", ex)
      }
    }

    log.info("Finished running scheduled metrics job")
  }
}
