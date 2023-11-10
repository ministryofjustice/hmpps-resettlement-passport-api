package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import io.micrometer.core.instrument.search.MeterNotFoundException
import io.mockk.Awaits
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doCallRealMethod
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.MetricsService
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class MetricsIntegrationTest : IntegrationTestBase() {

  @Autowired
  protected lateinit var metricsService: MetricsService

  @Autowired
  protected lateinit var registry: MeterRegistry

  private val fakeNow = LocalDate.parse("2023-01-01")

  @Test
  fun `test collect metrics - blank database and no results from prisoner search api`() = runTest {

    prisonRegisterApiMockServer.stubPrisonList(200)
    offenderSearchApiMockServer.stubGetPrisonersList("MDI", "", 500, 0, 404)

    metricsService.recordCustomMetrics()
    assertThrows<MeterNotFoundException> { registry.get("total_prisoners_count").gauges() }
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-4.sql")
  fun `test collect metrics - happy path`() = runTest {

    mockkStatic(LocalDate::class)
    every { LocalDate.now() } returns fakeNow

    prisonRegisterApiMockServer.stubPrisonList(200)
    offenderSearchApiMockServer.stubGetPrisonersList("MDI", "", 500, 0, 200)

    metricsService.recordCustomMetrics()

    registry.meters.filter { it.id.name.endsWith("_prisoners_count") }.forEach { meter ->
      meter.measure().forEach { println("name=${meter.id.name}, tags=${meter.id.tags}, value=${it.value}") }
    }

    Assertions.assertEquals(3.0, registry.get("total_prisoners_count").tags("prison", "Moorland (HMP & YOI)", "releaseDate", "Overall").gauge().value())
    Assertions.assertEquals(0.0, registry.get("total_prisoners_count").tags("prison", "Moorland (HMP & YOI)", "releaseDate", "12 Weeks").gauge().value())
    Assertions.assertEquals(0.0, registry.get("total_prisoners_count").tags("prison", "Moorland (HMP & YOI)", "releaseDate", "24 Weeks").gauge().value())

    Assertions.assertEquals(0.0, registry.get("total_not_started_prisoners_count").tags("prison", "Moorland (HMP & YOI)", "releaseDate", "Overall").gauge().value())
    Assertions.assertEquals(0.0, registry.get("total_not_started_prisoners_count").tags("prison", "Moorland (HMP & YOI)", "releaseDate", "12 Weeks").gauge().value())
    Assertions.assertEquals(0.0, registry.get("total_not_started_prisoners_count").tags("prison", "Moorland (HMP & YOI)", "releaseDate", "24 Weeks").gauge().value())

    Assertions.assertEquals(0.0, registry.get("total_in_progress_prisoners_count").tags("prison", "Moorland (HMP & YOI)", "releaseDate", "Overall").gauge().value())
    Assertions.assertEquals(0.0, registry.get("total_in_progress_prisoners_count").tags("prison", "Moorland (HMP & YOI)", "releaseDate", "12 Weeks").gauge().value())
    Assertions.assertEquals(0.0, registry.get("total_in_progress_prisoners_count").tags("prison", "Moorland (HMP & YOI)", "releaseDate", "24 Weeks").gauge().value())

    Assertions.assertEquals(0.0, registry.get("total_done_prisoners_count").tags("prison", "Moorland (HMP & YOI)", "releaseDate", "Overall").gauge().value())
    Assertions.assertEquals(0.0, registry.get("total_done_prisoners_count").tags("prison", "Moorland (HMP & YOI)", "releaseDate", "12 Weeks").gauge().value())
    Assertions.assertEquals(0.0, registry.get("total_done_prisoners_count").tags("prison", "Moorland (HMP & YOI)", "releaseDate", "24 Weeks").gauge().value())

    unmockkAll()
  }

}