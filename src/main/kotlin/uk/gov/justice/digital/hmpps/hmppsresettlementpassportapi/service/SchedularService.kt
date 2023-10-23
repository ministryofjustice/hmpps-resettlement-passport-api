package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class SchedularService(
  val metricsService: MetricsService,
) {

  //@Scheduled(fixedDelay = 1800000)
  @Scheduled(cron = "0 0/30 * * * ?")
  fun metricsScheduledTask() {
    runBlocking {
      metricsService.recordPrisonersCountForEachPrison()
    }
  }
}
