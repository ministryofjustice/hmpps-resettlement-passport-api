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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonRegisterApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonerSearchApiService
import java.time.LocalDate

@Service
class POPUserMetricsService(
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val prisonRegisterApiService: PrisonRegisterApiService,
  private val prisonerService: PrisonerService,
  private val registry: MeterRegistry,
  private val prisonOTPService: PoPUserOTPService
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun recordCustomMetrics() {
    recordProbationUsersMetrics(null.toString())
  }

  fun recordProbationUsersMetrics(prisonId: String): Int {
    val otpList = prisonOTPService.getAllOTPs()
    val cnt = otpList?.size ?: 0
    registry.gauge(
        Met,
        cnt,
      )
    return cnt
  }
}
