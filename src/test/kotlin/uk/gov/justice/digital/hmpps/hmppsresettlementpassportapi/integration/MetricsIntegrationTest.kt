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
  fun `test get metrics - happy path`() {
    val prisonId = "MDI"
    val expectedOutput = readFile("testdata/expectation/prisoner-counts.json")

    mockkStatic(LocalDate::class)
    every { LocalDate.now() } returns fakeNow

    seedPathwayStatuses()

    prisonRegisterApiMockServer.stubPrisonList(200)
    offenderSearchApiMockServer.stubGetPrisonersList("testdata/offender-search-api/prisoner-offender-search-3.json", prisonId, "", 500, 0, 200)

    webTestClient.get()
      .uri("/resettlement-passport/metrics/prisoner-counts?prisonId=$prisonId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)

    Assertions.assertEquals(
      5.0,
      registry.get("total_prisoners_count")
        .tags("prison", "Moorland (HMP & YOI)", "status", "All", "releaseDate", "Past").gauge()
        .value(),
    )
    Assertions.assertEquals(
      5.0,
      registry.get("total_prisoners_count")
        .tags("prison", "Moorland (HMP & YOI)", "status", "All", "releaseDate", "All Future").gauge()
        .value(),
    )
    Assertions.assertEquals(
      1.0,
      registry.get("total_prisoners_count")
        .tags("prison", "Moorland (HMP & YOI)", "status", "All", "releaseDate", "12 Weeks").gauge()
        .value(),
    )
    Assertions.assertEquals(
      1.0,
      registry.get("total_prisoners_count")
        .tags("prison", "Moorland (HMP & YOI)", "status", "All", "releaseDate", "24 Weeks").gauge()
        .value(),
    )

    Assertions.assertEquals(
      6.0,
      registry.get("total_prisoners_count")
        .tags("prison", "Moorland (HMP & YOI)", "status", "Not Started", "releaseDate", "Past").gauge()
        .value(),
    )
    Assertions.assertEquals(
      42.0,
      registry.get("total_prisoners_count")
        .tags("prison", "Moorland (HMP & YOI)", "status", "Not Started", "releaseDate", "All Future")
        .gauge().value(),
    )
    Assertions.assertEquals(
      9.0,
      registry.get("total_prisoners_count")
        .tags("prison", "Moorland (HMP & YOI)", "status", "Not Started", "releaseDate", "12 Weeks").gauge().value(),
    )
    Assertions.assertEquals(
      21.0,
      registry.get("total_prisoners_count")
        .tags("prison", "Moorland (HMP & YOI)", "status", "Not Started", "releaseDate", "24 Weeks").gauge().value(),
    )

    Assertions.assertEquals(
      4.0,
      registry.get("total_prisoners_count")
        .tags("prison", "Moorland (HMP & YOI)", "status", "In Progress", "releaseDate", "Past")
        .gauge().value(),
    )
    Assertions.assertEquals(
      28.0,
      registry.get("total_prisoners_count")
        .tags("prison", "Moorland (HMP & YOI)", "status", "In Progress", "releaseDate", "All Future")
        .gauge().value(),
    )
    Assertions.assertEquals(
      6.0,
      registry.get("total_prisoners_count")
        .tags("prison", "Moorland (HMP & YOI)", "status", "In Progress", "releaseDate", "12 Weeks").gauge().value(),
    )
    Assertions.assertEquals(
      14.0,
      registry.get("total_prisoners_count")
        .tags("prison", "Moorland (HMP & YOI)", "status", "In Progress", "releaseDate", "24 Weeks").gauge().value(),
    )

    Assertions.assertEquals(
      8.0,
      registry.get("total_prisoners_count")
        .tags("prison", "Moorland (HMP & YOI)", "status", "Done", "releaseDate", "Past")
        .gauge().value(),
    )
    Assertions.assertEquals(
      56.0,
      registry.get("total_prisoners_count")
        .tags("prison", "Moorland (HMP & YOI)", "status", "Done", "releaseDate", "All Future")
        .gauge().value(),
    )
    Assertions.assertEquals(
      12.0,
      registry.get("total_prisoners_count")
        .tags("prison", "Moorland (HMP & YOI)", "status", "Done", "releaseDate", "12 Weeks")
        .gauge().value(),
    )
    Assertions.assertEquals(
      28.0,
      registry.get("total_prisoners_count")
        .tags("prison", "Moorland (HMP & YOI)", "status", "Done", "releaseDate", "24 Weeks")
        .gauge().value(),
    )

    unmockkAll()
  }

  @Test
  fun `test get metrics - 400 (missing prisonId)`() {
    webTestClient.get()
      .uri("/resettlement-passport/metrics/prisoner-counts")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType("application/json")
      .expectBody().jsonPath("status").isEqualTo(400)
  }

  @Test
  fun `test get metrics - 404 (not existent prisonId)`() {
    webTestClient.get()
      .uri("/resettlement-passport/metrics/prisoner-counts?prisonId=ABC")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody().jsonPath("status").isEqualTo(404)
  }

  @Test
  fun `test get metrics - 401`() {
    webTestClient.get()
      .uri("/resettlement-passport/metrics/prisoner-counts?prisonId=MDI")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `test get metrics - 403`() {
    webTestClient.get()
      .uri("/resettlement-passport/metrics/prisoner-counts?prisonId=MDI")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType("application/json")
      .expectBody().jsonPath("status").isEqualTo(403)
  }

  private fun seedPathwayStatuses() {
    val uniqueId = AtomicInteger()

    // Note - we have set today to 1st Jan 2023 for this test
    val releaseDates = listOf(
      // In past
      LocalDate.parse("2022-11-01"),
      LocalDate.parse("2022-12-23"),

      // Within 12 weeks
      LocalDate.parse("2023-01-01"), // Today should be included in the future
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

  @Test
  fun `get metrics for MDI - happy path`() {
  }
}
