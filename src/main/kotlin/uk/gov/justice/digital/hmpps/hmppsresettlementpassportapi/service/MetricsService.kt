package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.OffenderSearchApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonRegisterApiService
import java.time.LocalDate

@Service
class MetricsService(
  private val offenderSearchApiService: OffenderSearchApiService,
  private val prisonRegisterApiService: PrisonRegisterApiService,
  private val registry: MeterRegistry,

) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    private var prisonersCountMap = HashMap<String, Int>()
  }

  suspend fun recordCustomMetrics() {
    registry.clear()
    recordPrisonersCountForEachPrison()
  }
  private suspend fun recordPrisonersCountForEachPrison() {
    val prisonList = prisonRegisterApiService.getActivePrisonsList()
    val earliestReleaseDate = LocalDate.now().minusDays(1)
    val latestRD12Weeks = LocalDate.now().plusDays(84)
    val latestRD24Weeks = LocalDate.now().plusDays(168)
    for (item in prisonList) {
      var prisonersCount = 0
      var prisoners12WeeksCount = 0
      var prisoners24WeeksCount = 0
      var prisonersAllTimeCount = 0
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
              prisonersCount++
              if (it.displayReleaseDate != null && (it.displayReleaseDate!! > earliestReleaseDate || it.displayReleaseDate!! < latestRD12Weeks)) {
                prisoners12WeeksCount++
              }
              if (it.displayReleaseDate != null && (it.displayReleaseDate!! > earliestReleaseDate || it.displayReleaseDate!! < latestRD24Weeks)) {
                prisoners24WeeksCount++
              }
              if (it.displayReleaseDate != null && (it.displayReleaseDate!! < earliestReleaseDate)) {
                prisonersAllTimeCount++
              }
              prisonersCountMap["total_prisoners_count_${it.prisonId}"] = prisonersCount
              prisonersCountMap["total_prisoners_12Weeks_count_${it.prisonId}"] = prisoners12WeeksCount
              prisonersCountMap["total_prisoners_24Weeks_count_${it.prisonId}"] = prisoners24WeeksCount
              prisonersCountMap["total_prisoners_AllTime_count_${it.prisonId}"] = prisonersAllTimeCount
            }

            prisonersCountMap["total_prisoners_count_${item.id}"]?.let { it1 ->
              registry.gauge(
                "total_prisoners_count",
                Tags.of("prison", item.name),
                it1,
              )
            }
            prisonersCountMap["total_prisoners_12Weeks_count_${item.id}"]?.let { it1 ->
              registry.gauge(
                "total_prisoners_12Weeks_count",
                Tags.of("prison", item.name),
                it1,
              )
            }
            prisonersCountMap["total_prisoners_24Weeks_count_${item.id}"]?.let { it1 ->
              registry.gauge(
                "total_prisoners_24Weeks_count",
                Tags.of("prison", item.name),
                it1,
              )
            }
            prisonersCountMap["total_prisoners_AllTime_count_${item.id}"]?.let { it1 ->
              registry.gauge(
                "total_prisoners_AllTime_count",
                Tags.of("prison", item.name),
                it1,
              )
            }
          }
        }
      } catch (ex: Exception) {
        log.warn("Prisoners data not found. Unable to fetch Prisoners for the Prison Id ${item.id}")
      }
    }
  }
}
