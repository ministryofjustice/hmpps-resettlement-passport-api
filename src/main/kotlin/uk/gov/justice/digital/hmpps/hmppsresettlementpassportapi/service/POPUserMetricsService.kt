package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonRegisterApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonerSearchApiService

@Service
class POPUserMetricsService(
  private val registry: MeterRegistry,
  private val prisonOTPService: PoPUserOTPService,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun recordCustomMetrics() {
    recordProbationUsersMetrics()
  }

  fun recordProbationUsersMetrics(): Int {
    val otpList = prisonOTPService.getAllOTPs()
    val cnt = otpList?.size ?: 0
    registry.gauge(
      "total_pack_print_count",
      cnt,
    )
    return cnt
  }
}
