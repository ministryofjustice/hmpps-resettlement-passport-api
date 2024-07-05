package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.health

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.health.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ClamAVConfig
import xyz.capybara.clamav.ClamavClient
import xyz.capybara.clamav.ClamavException
import java.net.ConnectException

class ClamAVHealthTest {
  private val clamAVConfig = mockk<ClamAVConfig>()
  private val clamavClient = mockk<ClamavClient>()
  private val meterRegistry = mockk<MeterRegistry>()

  @Test
  fun `ping is called when clamAvEnabled`() {
    every { clamAVConfig.clamavClient() } returns clamavClient
    every { clamavClient.ping() } just runs
    every { meterRegistry.gauge("upstream_healthcheck", any<Iterable<Tag>>(), any<Number>()) } returns mockk()

    val clamAVHealth = ClamAVHealth(true, clamAVConfig, meterRegistry)
    val response = clamAVHealth.health()

    assertThat(response.status, equalTo(Status.UP))

    verify { clamavClient.ping() }
    verify { meterRegistry.gauge("upstream_healthcheck", Tags.of("service", "clamAV"), 1) }
  }

  @Test
  fun `ping is not called when clamAvEnabled=false`() {
    every { clamAVConfig.clamavClient() } returns clamavClient
    every { meterRegistry.gauge("upstream_healthcheck", any<Iterable<Tag>>(), any<Number>()) } returns mockk()

    val clamAVHealth = ClamAVHealth(false, clamAVConfig, meterRegistry)
    val response = clamAVHealth.health()

    assertThat(response.status, equalTo(Status.UP))

    verify(exactly = 0) { clamavClient.ping() }
  }

  @Test
  fun `Status is Down when ping fails`() {
    every { clamAVConfig.clamavClient() } returns clamavClient
    every { clamavClient.ping() } throws ClamavException(ConnectException("FAIL"))
    every { meterRegistry.gauge("upstream_healthcheck", any<Iterable<Tag>>(), any<Number>()) } returns mockk()

    val clamAVHealth = ClamAVHealth(true, clamAVConfig, meterRegistry)
    val response = clamAVHealth.health()

    assertThat(response.status, equalTo(Status.DOWN))

    verify { clamavClient.ping() }
    verify { meterRegistry.gauge("upstream_healthcheck", Tags.of("service", "clamAV"), 0) }
  }
}
