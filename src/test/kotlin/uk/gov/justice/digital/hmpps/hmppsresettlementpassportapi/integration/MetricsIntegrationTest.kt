package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.search.MeterNotFoundException
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.StatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.MetricsService
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class MetricsIntegrationTest : IntegrationTestBase() {

  @Autowired
  protected lateinit var metricsService: MetricsService

  @Autowired
  protected lateinit var pathwayStatusRepository: PathwayStatusRepository

  @Autowired
  protected lateinit var prisonerRepository: PrisonerRepository

  @Autowired
  protected lateinit var registry: MeterRegistry

  private val fakeNow = LocalDate.parse("2023-01-01")

  @Test
  fun `test collect metrics - blank database and no results from prisoner search api`() {
    prisonRegisterApiMockServer.stubPrisonList(200)
    offenderSearchApiMockServer.stubGetPrisonersList("MDI", "", 500, 0, 404)

    metricsService.recordCustomMetrics()
    assertThrows<MeterNotFoundException> { registry.get("total_prisoners_count").gauges() }
  }

  @Test
  fun `test collect metrics - happy path`() {
    mockkStatic(LocalDate::class)
    every { LocalDate.now() } returns fakeNow

    seedPathwayStatuses()

    prisonRegisterApiMockServer.stubPrisonList(200)
    offenderSearchApiMockServer.stubGetPrisonersList("MDI", "", 500, 0, 200)

    metricsService.recordCustomMetrics()

    Assertions.assertEquals(
      4.0,
      registry.get("total_prisoners_count").tags("prison", "Moorland (HMP & YOI)", "releaseDate", "Overall").gauge()
        .value(),
    )
    Assertions.assertEquals(
      0.0,
      registry.get("total_prisoners_count").tags("prison", "Moorland (HMP & YOI)", "releaseDate", "12 Weeks").gauge()
        .value(),
    )
    Assertions.assertEquals(
      0.0,
      registry.get("total_prisoners_count").tags("prison", "Moorland (HMP & YOI)", "releaseDate", "24 Weeks").gauge()
        .value(),
    )

    Assertions.assertEquals(
      42.0,
      registry.get("total_not_started_prisoners_count").tags("prison", "Moorland (HMP & YOI)", "releaseDate", "Overall")
        .gauge().value(),
    )
    Assertions.assertEquals(
      9.0,
      registry.get("total_not_started_prisoners_count")
        .tags("prison", "Moorland (HMP & YOI)", "releaseDate", "12 Weeks").gauge().value(),
    )
    Assertions.assertEquals(
      21.0,
      registry.get("total_not_started_prisoners_count")
        .tags("prison", "Moorland (HMP & YOI)", "releaseDate", "24 Weeks").gauge().value(),
    )

    Assertions.assertEquals(
      28.0,
      registry.get("total_in_progress_prisoners_count").tags("prison", "Moorland (HMP & YOI)", "releaseDate", "Overall")
        .gauge().value(),
    )
    Assertions.assertEquals(
      6.0,
      registry.get("total_in_progress_prisoners_count")
        .tags("prison", "Moorland (HMP & YOI)", "releaseDate", "12 Weeks").gauge().value(),
    )
    Assertions.assertEquals(
      14.0,
      registry.get("total_in_progress_prisoners_count")
        .tags("prison", "Moorland (HMP & YOI)", "releaseDate", "24 Weeks").gauge().value(),
    )

    Assertions.assertEquals(
      56.0,
      registry.get("total_done_prisoners_count").tags("prison", "Moorland (HMP & YOI)", "releaseDate", "Overall")
        .gauge().value(),
    )
    Assertions.assertEquals(
      12.0,
      registry.get("total_done_prisoners_count").tags("prison", "Moorland (HMP & YOI)", "releaseDate", "12 Weeks")
        .gauge().value(),
    )
    Assertions.assertEquals(
      28.0,
      registry.get("total_done_prisoners_count").tags("prison", "Moorland (HMP & YOI)", "releaseDate", "24 Weeks")
        .gauge().value(),
    )

    unmockkAll()
  }

  private fun seedPathwayStatuses() {
    val uniqueId = AtomicInteger()

    val releaseDates = listOf(
      // In past
      LocalDate.parse("2022-11-01"),
      LocalDate.parse("2022-12-23"),

      // Within 12 weeks
      LocalDate.parse("2023-01-02"),
      LocalDate.parse("2023-03-20"),
      LocalDate.parse("2023-02-28"),

      // Within 24 weeks
      LocalDate.parse("2023-04-04"),
      LocalDate.parse("2023-05-30"),
      LocalDate.parse("2023-04-28"),
      LocalDate.parse("2023-06-13"),

      // Beyond 24 weeks
      LocalDate.parse("2024-04-04"),
      LocalDate.parse("2023-10-13"),
      LocalDate.parse("2050-12-17"),
      LocalDate.parse("2033-09-10"),
      LocalDate.parse("2023-11-30"),

      // null
      null,
      null,
    )

    val statusPatterns = listOf(
      // Not Started
      listOf(1, 1, 1, 1, 1, 1, 1),
      listOf(1, 1, 1, 1, 1, 1, 1),
      listOf(1, 1, 1, 1, 1, 1, 1),
      // In Progress
      listOf(3, 1, 3, 5, 4, 2, 2),
      listOf(3, 1, 3, 5, 4, 2, 2),
      // Done
      listOf(3, 4, 3, 3, 3, 3, 3),
      listOf(3, 3, 3, 3, 4, 3, 3),
      listOf(3, 4, 3, 3, 3, 3, 4),
      listOf(5, 5, 5, 5, 5, 5, 5),
    )

    for (releaseDate in releaseDates) {
      for (i in 1..statusPatterns.size) {
        val prisoner = prisonerRepository.save(
          PrisonerEntity(
            null,
            "${uniqueId.incrementAndGet()}",
            LocalDateTime.now(),
            "${uniqueId.incrementAndGet()}",
            "MDI",
            releaseDate,
          ),
        )
        statusPatterns[i - 1].forEachIndexed { pathwayId, statusId ->
          pathwayStatusRepository.save(
            PathwayStatusEntity(
              null,
              prisoner,
              PathwayEntity(pathwayId.toLong() + 1, "", true, LocalDateTime.now()),
              StatusEntity(statusId.toLong(), "", true, LocalDateTime.now()),
              LocalDateTime.now(),
            ),
          )
        }
      }
    }
  }
}
