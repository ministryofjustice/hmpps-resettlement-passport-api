package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.WatchlistEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.WatchlistRepository
import java.time.LocalDate
import java.time.LocalDateTime

class WatchlistIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var watchlistRepository: WatchlistRepository

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-3.sql")
  fun `Create watchlist - happy path`() {
    val nomsId = "ABC1234"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/watch")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk

    val expectedWatchlistEntry = listOf(
      WatchlistEntity(
        id = 1,
        prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-17T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")),
        staffUsername = "RESETTLEMENTPASSPORT_ADM",
        creationDate = LocalDateTime.now(),
      ),
    )
    val actualWatchlistEntry = watchlistRepository.findAll()
    assertThat(actualWatchlistEntry).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(expectedWatchlistEntry)
  }

  @Test
  fun `Create watchlist - nomsId not found in database`() {
    val nomsId = "ABC1234"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/watch")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `Create watchlist - Cannot get name from auth token`() {
    val nomsId = "ABC1234"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/watch")
      .exchange()
      .expectStatus().isUnauthorized
  }
}
