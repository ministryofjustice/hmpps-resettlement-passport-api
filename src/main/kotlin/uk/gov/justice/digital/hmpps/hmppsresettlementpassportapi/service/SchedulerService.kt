package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class SchedulerService(
  val poPUserOTPService: PoPUserOTPService,
) {

  @Scheduled(cron = "\${schedule.expression.delete-expired-otp}")
  fun deleteExpiredOTPScheduledTask() {
    poPUserOTPService.deleteExpiredPoPUserOTP()
  }
}
