package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockkStatic
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.BankApplication
import java.time.LocalDateTime

class BankApplicationIntegrationTest : IntegrationTestBase() {

  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @Test
  @Sql("classpath:testdata/sql/seed-bank-application.sql")
  fun `Create, update and delete bank application- happy path`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val expectedOutput = readFile("testdata/expectation/bank-application.json")
    val expectedOutput2 = readFile("testdata/expectation/bank-application2.json")

    val nomsId = "123"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/bankapplication")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/bankapplication")
      .bodyValue(
        BankApplication(applicationSubmittedDate = fakeNow, bankName = "Lloyds"),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)

    webTestClient.patch()
      .uri("/resettlement-passport/prisoner/$nomsId/bankapplication/1")
      .bodyValue(
        BankApplication(resubmissionDate = fakeNow, bankResponseDate = fakeNow, status = "Account opened"),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput2)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/bankapplication")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(expectedOutput2)

    webTestClient.delete()
      .uri("/resettlement-passport/prisoner/$nomsId/bankapplication/1")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/bankapplication")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound

    val auditQueueMessages = sqsClient.receiveMessage(ReceiveMessageRequest.builder().queueUrl(auditQueueUrl).maxNumberOfMessages(3).build()).get().messages()
    assertThat(ObjectMapper().readValue(auditQueueMessages[0].body(), Map::class.java))
      .usingRecursiveComparison()
      .ignoringFields("when")
      .isEqualTo(mapOf("correlationId" to null, "details" to null, "service" to "hmpps-resettlement-passport-api", "subjectId" to "123", "subjectType" to "PRISONER_ID", "what" to "CREATE_BANK_APPLICATION", "when" to "2025-01-06T13:48:20.391273Z", "who" to "RESETTLEMENTPASSPORT_ADM"))
    assertThat(ObjectMapper().readValue(auditQueueMessages[1].body(), Map::class.java))
      .usingRecursiveComparison()
      .ignoringFields("when")
      .isEqualTo(mapOf("correlationId" to null, "details" to null, "service" to "hmpps-resettlement-passport-api", "subjectId" to "123", "subjectType" to "PRISONER_ID", "what" to "UPDATE_BANK_APPLICATION", "when" to "2025-01-06T13:48:20.391273Z", "who" to "RESETTLEMENTPASSPORT_ADM"))
    assertThat(ObjectMapper().readValue(auditQueueMessages[2].body(), Map::class.java))
      .usingRecursiveComparison()
      .ignoringFields("when")
      .isEqualTo(mapOf("correlationId" to null, "details" to null, "service" to "hmpps-resettlement-passport-api", "subjectId" to "123", "subjectType" to "PRISONER_ID", "what" to "DELETE_BANK_APPLICATION", "when" to "2025-01-06T13:48:20.391273Z", "who" to "RESETTLEMENTPASSPORT_ADM"))
  }
}
