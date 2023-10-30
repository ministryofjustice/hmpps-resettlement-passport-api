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
  private val prisonerService: PrisonerService,
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
      var activePrisonersCount = 0
      var activePrisoners12WeeksCount = 0
      var activePrisoners24WeeksCount = 0
      var activePrisonersAllTimeCount = 0
      var inusePrisonersCount = 0
      var inusePrisoners12WeeksCount = 0
      var inusePrisoners24WeeksCount = 0
      var inusePrisonersAllTimeCount = 0
      try {
        if (item.active) {
          val activePrisonersList = prisonerService.getActiveNomisIdsByPrisonId(item.id)
          val inusePrisonersList = prisonerService.getInUseNomisIdsByPrisonId(item.id)
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
              if (activePrisonersList.contains(it.prisonerNumber)) {
                activePrisonersCount++
              }
              if (inusePrisonersList.contains(it.prisonerNumber)) {
                inusePrisonersCount++
              }
              if (it.displayReleaseDate != null && (it.displayReleaseDate!! > earliestReleaseDate && it.displayReleaseDate!! <= latestRD12Weeks)) {
                prisoners12WeeksCount++
                if (activePrisonersList.contains(it.prisonerNumber)) {
                  activePrisoners12WeeksCount++
                }
                if (inusePrisonersList.contains(it.prisonerNumber)) {
                  inusePrisoners12WeeksCount++
                }
              }
              if (it.displayReleaseDate != null && (it.displayReleaseDate!! > earliestReleaseDate && (it.displayReleaseDate!! > latestRD12Weeks && it.displayReleaseDate!! <= latestRD24Weeks))) {
                prisoners24WeeksCount++
                if (activePrisonersList.contains(it.prisonerNumber)) {
                  activePrisoners24WeeksCount++
                }
                if (inusePrisonersList.contains(it.prisonerNumber)) {
                  inusePrisoners24WeeksCount++
                }
              }

              if (it.displayReleaseDate == null || (it.displayReleaseDate != null && (it.displayReleaseDate!! <= earliestReleaseDate || it.displayReleaseDate!! > latestRD24Weeks))) {
                prisonersAllTimeCount++
                if (activePrisonersList.contains(it.prisonerNumber)) {
                  activePrisonersAllTimeCount++
                }
                if (inusePrisonersList.contains(it.prisonerNumber)) {
                  inusePrisonersAllTimeCount++
                }
              }
            }
          }

          prisonersCountMap["total_inuse_prisoners_count_${item.id}"] = inusePrisonersCount
          prisonersCountMap["total_inuse_prisoners_12Weeks_count_${item.id}"] = inusePrisoners12WeeksCount
          prisonersCountMap["total_inuse_prisoners_24Weeks_count_${item.id}"] = inusePrisoners24WeeksCount
          prisonersCountMap["total_inuse_prisoners_AllTime_count_${item.id}"] = inusePrisonersAllTimeCount

          prisonersCountMap["total_active_prisoners_count_${item.id}"] = activePrisonersCount
          prisonersCountMap["total_active_prisoners_12Weeks_count_${item.id}"] = activePrisoners12WeeksCount
          prisonersCountMap["total_active_prisoners_24Weeks_count_${item.id}"] = activePrisoners24WeeksCount
          prisonersCountMap["total_active_prisoners_AllTime_count_${item.id}"] = activePrisonersAllTimeCount

          prisonersCountMap["total_prisoners_count_${item.id}"] = prisonersCount
          prisonersCountMap["total_prisoners_12Weeks_count_${item.id}"] = prisoners12WeeksCount
          prisonersCountMap["total_prisoners_24Weeks_count_${item.id}"] = prisoners24WeeksCount
          prisonersCountMap["total_prisoners_AllTime_count_${item.id}"] = prisonersAllTimeCount

          val tag1 = Tags.of("prison", item.name)
          prisonersCountMap["total_prisoners_count_${item.id}"]?.let { it1 ->
            registry.gauge(
              "total_prisoners_count",
              tag1.and("releaseDate", "Overall"),
              it1,
            )
          }

          val tag2 = Tags.of("prison", item.name)

          prisonersCountMap["total_prisoners_12Weeks_count_${item.id}"]?.let { it1 ->
            registry.gauge(
              "total_prisoners_12Weeks_count",
              tag2.and("releaseDate", "12 Weeks"),
              it1,
            )
          }
          val tag3 = Tags.of("prison", item.name)

          prisonersCountMap["total_prisoners_24Weeks_count_${item.id}"]?.let { it1 ->
            registry.gauge(
              "total_prisoners_24Weeks_count",
              tag3.and("releaseDate", "24 Weeks"),
              it1,
            )
          }
          val tag4 = Tags.of("prison", item.name)

          prisonersCountMap["total_prisoners_AllTime_count_${item.id}"]?.let { it1 ->
            registry.gauge(
              "total_prisoners_AllTime_count",
              tag4.and("releaseDate", "Past or Unknown"),
              it1,
            )
          }

          prisonersCountMap["total_active_prisoners_count_${item.id}"]?.let { it1 ->
            registry.gauge(
              "total_active_prisoners_count",
              tag1.and("releaseDate", "Overall"),
              it1,
            )
          }
          prisonersCountMap["total_active_prisoners_12Weeks_count_${item.id}"]?.let { it1 ->
            registry.gauge(
              "total_active_prisoners_12Weeks_count",
              tag2.and("releaseDate", "12 Weeks"),
              it1,
            )
          }
          prisonersCountMap["total_active_prisoners_24Weeks_count_${item.id}"]?.let { it1 ->
            registry.gauge(
              "total_active_prisoners_24Weeks_count",
              tag3.and("releaseDate", "24 Weeks"),
              it1,
            )
          }
          prisonersCountMap["total_active_prisoners_AllTime_count_${item.id}"]?.let { it1 ->
            registry.gauge(
              "total_active_prisoners_AllTime_count",
              tag4.and("releaseDate", "Past or Unknown"),
              it1,
            )
          }

          prisonersCountMap["total_inuse_prisoners_count_${item.id}"]?.let { it1 ->
            registry.gauge(
              "total_inuse_prisoners_count",
              tag1.and("releaseDate", "Overall"),
              it1,
            )
          }
          prisonersCountMap["total_inuse_prisoners_12Weeks_count_${item.id}"]?.let { it1 ->
            registry.gauge(
              "total_inuse_prisoners_12Weeks_count",
              tag2.and("releaseDate", "12 Weeks"),
              it1,
            )
          }
          prisonersCountMap["total_inuse_prisoners_24Weeks_count_${item.id}"]?.let { it1 ->
            registry.gauge(
              "total_inuse_prisoners_24Weeks_count",
              tag3.and("releaseDate", "24 Weeks"),
              it1,
            )
          }
          prisonersCountMap["total_inuse_prisoners_AllTime_count_${item.id}"]?.let { it1 ->
            registry.gauge(
              "total_inuse_prisoners_AllTime_count",
              tag4.and("releaseDate", "Past or Unknown"),
              it1,
            )
          }
        }
      } catch (ex: Exception) {
        log.warn("Prisoners data not found. Unable to fetch Prisoners for the Prison Id ${item.id}")
      }
    }
  }
}
