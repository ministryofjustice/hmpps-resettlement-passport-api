package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.WatchlistEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.WatchlistRepository
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
        prisonerId = 1,
        staffUsername = "RESETTLEMENTPASSPORT_ADM",
        creationDate = LocalDateTime.now(),
      ),
    )
    val actualWatchlistEntry = watchlistRepository.findAll()
    assertThat(actualWatchlistEntry).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(expectedWatchlistEntry)

    val auditQueueMessage = sqsClient.receiveMessage(ReceiveMessageRequest.builder().queueUrl(auditQueueUrl).build()).get().messages()[0]
    assertThat(ObjectMapper().readValue(auditQueueMessage.body(), Map::class.java))
      .usingRecursiveComparison()
      .ignoringFields("when")
      .isEqualTo(mapOf("correlationId" to null, "details" to null, "service" to "hmpps-resettlement-passport-api", "subjectId" to "ABC1234", "subjectType" to "PRISONER_ID", "what" to "CREATE_WATCH_LIST", "when" to "2025-01-06T13:48:20.391273Z", "who" to "RESETTLEMENTPASSPORT_ADM"))
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
  fun `Create watchlist - cannot get name from auth token (not permitted)`() {
    val nomsId = "ABC1234"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/watch")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  @Sql("classpath:testdata/sql/seed-watchlist.sql")
  fun `Delete watchlist - happy path`() {
    val nomsId = "ABC1234"

    webTestClient.delete()
      .uri("/resettlement-passport/prisoner/$nomsId/watch")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk

    val actualWatchlistEntry = watchlistRepository.findAll()
    assertThat(actualWatchlistEntry).isEmpty()

    val auditQueueMessage = sqsClient.receiveMessage(ReceiveMessageRequest.builder().queueUrl(auditQueueUrl).build()).get().messages()[0]
    assertThat(ObjectMapper().readValue(auditQueueMessage.body(), Map::class.java))
      .usingRecursiveComparison()
      .ignoringFields("when")
      .isEqualTo(mapOf("correlationId" to null, "details" to null, "service" to "hmpps-resettlement-passport-api", "subjectId" to "ABC1234", "subjectType" to "PRISONER_ID", "what" to "DELETE_WATCH_LIST", "when" to "2025-01-06T13:48:20.391273Z", "who" to "RESETTLEMENTPASSPORT_ADM"))
  }

  @Test
  @Sql("classpath:testdata/sql/seed-watchlist.sql")
  fun `Delete watchlist - cannot get name from auth token (not permitted)`() {
    val nomsId = "ABC1234"

    webTestClient.delete()
      .uri("/resettlement-passport/prisoner/$nomsId/watch")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Delete watchlist - nomsId not found in database`() {
    val nomsId = "ABC1234"
    webTestClient.delete()
      .uri("/resettlement-passport/prisoner/$nomsId/watch")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
  }
}
