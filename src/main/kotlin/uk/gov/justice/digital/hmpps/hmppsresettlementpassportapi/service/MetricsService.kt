package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service

@Service
class MetricsService(
  private val registry: MeterRegistry,
) {

  fun incrementCounter(metricName: String, vararg tags: String) {
    registry.counter(metricName, *tags).increment()
  }
}
