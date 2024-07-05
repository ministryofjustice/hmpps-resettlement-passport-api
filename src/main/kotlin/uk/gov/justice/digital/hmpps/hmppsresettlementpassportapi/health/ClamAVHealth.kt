package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.health

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ClamAVConfig

@Component("clamAV")
class ClamAVHealth(
  @Value("\${clamav.virus.scan.enabled:true}") val clamavEnabled: Boolean,
  @Autowired private val clamAVConfig: ClamAVConfig,
  @Autowired private val meterRegistry: MeterRegistry,
) : HealthIndicator {
  private val componentName = "clamAV"

  override fun health(): Health {
    return try {
      if (clamavEnabled) {
        clamAVConfig.clamavClient().ping()
      }
      meterRegistry.gauge("upstream_healthcheck", Tags.of("service", componentName), 1)
      Health.up().withDetail("active", clamavEnabled).build()
    } catch (e: Exception) {
      meterRegistry.gauge("upstream_healthcheck", Tags.of("service", componentName), 0)
      Health.down(e).build()
    }
  }
}
