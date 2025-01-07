package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockkStatic
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.IdApplicationPatch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.IdApplicationPost
import java.math.BigDecimal
import java.time.LocalDateTime

class IdApplicationIntegrationTest : IntegrationTestBase() {

  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @Test
  @Sql("classpath:testdata/sql/seed-bank-application.sql")
  fun `Create, update and delete bank application- happy path`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow

    val expectedOutput = readFile("testdata/expectation/id-application-post-result.json")
    val expectedOutput2 = readFile("testdata/expectation/id-application-patch-result.json")
    val expectedOutput3 = readFile("testdata/expectation/id-application-get-result.json")

    val nomsId = "123"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/idapplication/all")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json("[]")

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/idapplication")
      .bodyValue(
        IdApplicationPost(
          idType = "Birth certificate",
          applicationSubmittedDate = fakeNow,
          isPriorityApplication = false,
          costOfApplication = BigDecimal(10.50),
          haveGro = true,
          isUkNationalBornOverseas = false,
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)

    webTestClient.patch()
      .uri("/resettlement-passport/prisoner/$nomsId/idapplication/1")
      .bodyValue(
        IdApplicationPatch(
          status = "Accepted",
          dateIdReceived = fakeNow,
          addedToPersonalItemsDate = fakeNow,
          isAddedToPersonalItems = true,
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput2)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/idapplication/all")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(expectedOutput3)

    webTestClient.delete()
      .uri("/resettlement-passport/prisoner/$nomsId/idapplication/1")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/idapplication/all")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json("[]")

    val auditQueueMessages = sqsClient.receiveMessage(ReceiveMessageRequest.builder().queueUrl(auditQueueUrl).maxNumberOfMessages(3).build()).get().messages()
    assertThat(ObjectMapper().readValue(auditQueueMessages[0].body(), Map::class.java))
      .usingRecursiveComparison()
      .ignoringFields("when")
      .isEqualTo(mapOf("correlationId" to null, "details" to null, "service" to "hmpps-resettlement-passport-api", "subjectId" to "123", "subjectType" to "PRISONER_ID", "what" to "CREATE_ID_APPLICATION", "when" to "2025-01-06T13:48:20.391273Z", "who" to "RESETTLEMENTPASSPORT_ADM"))
    assertThat(ObjectMapper().readValue(auditQueueMessages[1].body(), Map::class.java))
      .usingRecursiveComparison()
      .ignoringFields("when")
      .isEqualTo(mapOf("correlationId" to null, "details" to null, "service" to "hmpps-resettlement-passport-api", "subjectId" to "123", "subjectType" to "PRISONER_ID", "what" to "UPDATE_ID_APPLICATION", "when" to "2025-01-06T13:48:20.391273Z", "who" to "RESETTLEMENTPASSPORT_ADM"))
    assertThat(ObjectMapper().readValue(auditQueueMessages[2].body(), Map::class.java))
      .usingRecursiveComparison()
      .ignoringFields("when")
      .isEqualTo(mapOf("correlationId" to null, "details" to null, "service" to "hmpps-resettlement-passport-api", "subjectId" to "123", "subjectType" to "PRISONER_ID", "what" to "DELETE_ID_APPLICATION", "when" to "2025-01-06T13:48:20.391273Z", "who" to "RESETTLEMENTPASSPORT_ADM"))
  }
}
