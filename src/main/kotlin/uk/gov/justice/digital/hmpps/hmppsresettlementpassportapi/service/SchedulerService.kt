package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class SchedulerService(
  val metricsService: MetricsService,
  val prisonerService: PrisonerService,
  val poPUserOTPService: PoPUserOTPService,
  val popUserMetricsService: PoPUserMetricsService,
  @Value("\${cron.release-dates.batch-size:200}") val batchSize: Int,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Scheduled(cron = "\${schedule.expression.custom-metrics}")
  fun metricsScheduledTask() {
    metricsService.recordCustomMetrics()
  }

  @Scheduled(cron = "\${schedule.expression.reconcile-release-dates}")
  fun reconcileReleaseDatesInDatabase() {
    log.info("Start updating prisoner entities with new release date")

    var slice =
      prisonerService.getSliceOfAllPrisoners(PageRequest.of(0, batchSize).withSort(Sort.by(Sort.Direction.ASC, "id")))
    log.info("Updating prisoner entities with new release date - Page ${slice.number + 1}")
    prisonerService.updateAndSaveNewReleaseDates(slice.get())

    while (slice.hasNext()) {
      slice = prisonerService.getSliceOfAllPrisoners((slice.nextPageable()))
      log.info("Updating prisoner entities with new release date - Page ${slice.number + 1}")
      prisonerService.updateAndSaveNewReleaseDates(slice.get())
    }

    log.info("Finished updating prisoner entities with new release date")
  }

  @Scheduled(cron = "\${schedule.expression.delete-expired-otp}")
  fun deleteExpiredOTPScheduledTask() {
    poPUserOTPService.deleteExpiredPoPUserOTP()
  }

  @Scheduled(cron = "\${schedule.expression.release-day-metrics}")
  fun metricsProbationUsersScheduledTask() {
    popUserMetricsService.recordReleaseDayProbationUserAppointmentsMetrics()
  }

  @Scheduled(cron = "\${schedule.expression.custom-popuser-metrics}")
  fun popUserMetricsScheduledTask() {
    popUserMetricsService.recordCustomMetrics()
  }
}
