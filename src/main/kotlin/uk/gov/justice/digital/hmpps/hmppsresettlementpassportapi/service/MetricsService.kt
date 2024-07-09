package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerCountMetric
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerCountMetrics
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerCountMetricsByReleaseDate
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerCounts
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ReleaseDateTag
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.StatusTag
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonerSearchApiService
import java.time.LocalDate

@Service
class MetricsService(
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val prisonerService: PrisonerService,
  private val registry: MeterRegistry,
) {

  private val prisonerCountMetrics = PrisonerCountMetrics()

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun recordCustomMetrics() {
    recordPrisonersCountForEachPrison()
  }

  private fun recordPrisonersCountForEachPrison() {
    log.info("Started running scheduled metrics job")

    val earliestPastReleaseDate = LocalDate.parse("1900-01-01")
    val latestPastReleaseDate = LocalDate.now().minusDays(1)

    val earliestFutureReleaseDate = LocalDate.now()
    val latestFutureReleaseDate = LocalDate.parse("9999-12-31")
    val latestRD12Weeks = LocalDate.now().plusDays(84)
    val latestRD24Weeks = LocalDate.now().plusDays(168)

    val prisonList = prisonerService.getActivePrisonsList()

    for (prison in prisonList) {
      try {
        val prisoners = prisonerSearchApiService.findPrisonersBySearchTerm(prison.id, "")
        prisoners.forEach { prisonerService.setDisplayedReleaseDate(it) }

        val allPrisonersPastCount = prisoners.filter { it.displayReleaseDate != null && it.displayReleaseDate!! < earliestFutureReleaseDate }.size
        val allPrisonersFutureCount = prisoners.filter { it.displayReleaseDate == null || it.displayReleaseDate!! >= earliestFutureReleaseDate }.size
        val allPrisoners12WeeksCount = prisoners.filter { it.displayReleaseDate != null && it.displayReleaseDate!! >= earliestFutureReleaseDate && it.displayReleaseDate!! <= latestRD12Weeks }.size
        val allPrisoners24WeeksCount = prisoners.filter { it.displayReleaseDate != null && it.displayReleaseDate!! >= earliestFutureReleaseDate && it.displayReleaseDate!! <= latestRD24Weeks }.size

        val notStartedPrisonersPastCount = prisonerService.getNotStartedPrisonersByPrisonId(prison.id, earliestPastReleaseDate, latestPastReleaseDate).filter { it.releaseDate != null }.size
        val notStartedPrisonersFutureCount = prisonerService.getNotStartedPrisonersByPrisonId(prison.id, earliestFutureReleaseDate, latestFutureReleaseDate).size
        val notStartedPrisoners12WeeksCount = prisonerService.getNotStartedPrisonersByPrisonId(prison.id, earliestFutureReleaseDate, latestRD12Weeks).filter { it.releaseDate != null }.size
        val notStartedPrisoners24WeeksCount = prisonerService.getNotStartedPrisonersByPrisonId(prison.id, earliestFutureReleaseDate, latestRD24Weeks).filter { it.releaseDate != null }.size

        val inProgressPrisonersPastCount = prisonerService.getInProgressPrisonersByPrisonId(prison.id, earliestPastReleaseDate, latestPastReleaseDate).filter { it.releaseDate != null }.size
        val inProgressPrisonersFutureCount = prisonerService.getInProgressPrisonersByPrisonId(prison.id, earliestFutureReleaseDate, latestFutureReleaseDate).size
        val inProgressPrisoners12WeeksCount = prisonerService.getInProgressPrisonersByPrisonId(prison.id, earliestFutureReleaseDate, latestRD12Weeks).filter { it.releaseDate != null }.size
        val inProgressPrisoners24WeeksCount = prisonerService.getInProgressPrisonersByPrisonId(prison.id, earliestFutureReleaseDate, latestRD24Weeks).filter { it.releaseDate != null }.size

        val donePrisonersPastCount = prisonerService.getDonePrisonersByPrisonId(prison.id, earliestPastReleaseDate, latestPastReleaseDate).filter { it.releaseDate != null }.size
        val donePrisonersFutureCount = prisonerService.getDonePrisonersByPrisonId(prison.id, earliestFutureReleaseDate, latestFutureReleaseDate).size
        val donePrisoners12WeeksCount = prisonerService.getDonePrisonersByPrisonId(prison.id, earliestFutureReleaseDate, latestRD12Weeks).filter { it.releaseDate != null }.size
        val donePrisoners24WeeksCount = prisonerService.getDonePrisonersByPrisonId(prison.id, earliestFutureReleaseDate, latestRD24Weeks).filter { it.releaseDate != null }.size

        val metrics = listOf(
          PrisonerCountMetric(StatusTag.ALL, ReleaseDateTag.PAST, allPrisonersPastCount),
          PrisonerCountMetric(StatusTag.ALL, ReleaseDateTag.ALL_FUTURE, allPrisonersFutureCount),
          PrisonerCountMetric(StatusTag.ALL, ReleaseDateTag.TWELVE_WEEKS, allPrisoners12WeeksCount),
          PrisonerCountMetric(StatusTag.ALL, ReleaseDateTag.TWENTY_FOUR_WEEKS, allPrisoners24WeeksCount),

          PrisonerCountMetric(StatusTag.NOT_STARTED, ReleaseDateTag.PAST, notStartedPrisonersPastCount),
          PrisonerCountMetric(StatusTag.NOT_STARTED, ReleaseDateTag.ALL_FUTURE, notStartedPrisonersFutureCount),
          PrisonerCountMetric(StatusTag.NOT_STARTED, ReleaseDateTag.TWELVE_WEEKS, notStartedPrisoners12WeeksCount),
          PrisonerCountMetric(StatusTag.NOT_STARTED, ReleaseDateTag.TWENTY_FOUR_WEEKS, notStartedPrisoners24WeeksCount),

          PrisonerCountMetric(StatusTag.IN_PROGRESS, ReleaseDateTag.PAST, inProgressPrisonersPastCount),
          PrisonerCountMetric(StatusTag.IN_PROGRESS, ReleaseDateTag.ALL_FUTURE, inProgressPrisonersFutureCount),
          PrisonerCountMetric(StatusTag.IN_PROGRESS, ReleaseDateTag.TWELVE_WEEKS, inProgressPrisoners12WeeksCount),
          PrisonerCountMetric(StatusTag.IN_PROGRESS, ReleaseDateTag.TWENTY_FOUR_WEEKS, inProgressPrisoners24WeeksCount),

          PrisonerCountMetric(StatusTag.DONE, ReleaseDateTag.PAST, donePrisonersPastCount),
          PrisonerCountMetric(StatusTag.DONE, ReleaseDateTag.ALL_FUTURE, donePrisonersFutureCount),
          PrisonerCountMetric(StatusTag.DONE, ReleaseDateTag.TWELVE_WEEKS, donePrisoners12WeeksCount),
          PrisonerCountMetric(StatusTag.DONE, ReleaseDateTag.TWENTY_FOUR_WEEKS, donePrisoners24WeeksCount),
        )
        prisonerCountMetrics.metrics[prison] = metrics

        val prisonTag = Tags.of("prison", prison.name)

        prisonerCountMetrics.metrics[prison]?.forEachIndexed { i, metric ->
          registry.gauge(
            "total_prisoners_count",
            prisonTag
              .and("status", metric.status.label)
              .and("releaseDate", metric.releaseDate.label),
            prisonerCountMetrics,
          ) {
            it.metrics[prison]?.get(i)?.value?.toDouble() ?: throw RuntimeException("Can't find value for metric $metric. This is likely a coding error!")
          }
        }
      } catch (_: ResourceNotFoundException) {
      } catch (ex: Exception) {
        log.warn("Error collecting metrics for prison ${prison.name}", ex)
      }
    }

    log.info("Finished running scheduled metrics job")
  }

  fun getMetricsByPrisonId(prisonId: String): PrisonerCountMetricsByReleaseDate {
    if (prisonerCountMetrics.metrics.isEmpty()) {
      recordPrisonersCountForEachPrison()
    }
    val metrics = prisonerCountMetrics.metrics.filter { it.key.id == prisonId }.values.firstOrNull()
    if (metrics != null) {
      return PrisonerCountMetricsByReleaseDate(
        twelveWeeks = PrisonerCounts(
          totalPopulation = getNumberFromMetrics(metrics, ReleaseDateTag.TWELVE_WEEKS, StatusTag.ALL),
          notStarted = getNumberFromMetrics(metrics, ReleaseDateTag.TWELVE_WEEKS, StatusTag.NOT_STARTED),
          inProgress = getNumberFromMetrics(metrics, ReleaseDateTag.TWELVE_WEEKS, StatusTag.IN_PROGRESS),
          done = getNumberFromMetrics(metrics, ReleaseDateTag.TWELVE_WEEKS, StatusTag.DONE),
        ),
        twentyFourWeeks = PrisonerCounts(
          totalPopulation = getNumberFromMetrics(metrics, ReleaseDateTag.TWENTY_FOUR_WEEKS, StatusTag.ALL),
          notStarted = getNumberFromMetrics(metrics, ReleaseDateTag.TWENTY_FOUR_WEEKS, StatusTag.NOT_STARTED),
          inProgress = getNumberFromMetrics(metrics, ReleaseDateTag.TWENTY_FOUR_WEEKS, StatusTag.IN_PROGRESS),
          done = getNumberFromMetrics(metrics, ReleaseDateTag.TWENTY_FOUR_WEEKS, StatusTag.DONE),
        ),
        allFuture = PrisonerCounts(
          totalPopulation = getNumberFromMetrics(metrics, ReleaseDateTag.ALL_FUTURE, StatusTag.ALL),
          notStarted = getNumberFromMetrics(metrics, ReleaseDateTag.ALL_FUTURE, StatusTag.NOT_STARTED),
          inProgress = getNumberFromMetrics(metrics, ReleaseDateTag.ALL_FUTURE, StatusTag.IN_PROGRESS),
          done = getNumberFromMetrics(metrics, ReleaseDateTag.ALL_FUTURE, StatusTag.DONE),
        ),
      )
    } else {
      throw ResourceNotFoundException("No metrics found for prison [$prisonId]")
    }
  }

  fun getNumberFromMetrics(metrics: List<PrisonerCountMetric>, releaseDateTag: ReleaseDateTag, statusTag: StatusTag): Int? = metrics.firstOrNull { it.releaseDate == releaseDateTag && it.status == statusTag }?.value

  fun incrementCounter(metricName: String, vararg tags: String) {
    registry.counter(metricName, *tags).increment()
  }
}
