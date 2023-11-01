package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class SchedulerService(
  val metricsService: MetricsService,
  val prisonerService: PrisonerService,
) {

  @Scheduled(cron = "0 0/15 * * * ?")
  fun metricsScheduledTask() {
    runBlocking {
      metricsService.recordCustomMetrics()
    }
  }

  @Scheduled(initialDelay = 30000, fixedDelay = Long.MAX_VALUE)
  fun oneTimeScheduledTask() {
    runBlocking {
      prisonerService.updatePrisonIdInPrisoners()
    }
  }
}
