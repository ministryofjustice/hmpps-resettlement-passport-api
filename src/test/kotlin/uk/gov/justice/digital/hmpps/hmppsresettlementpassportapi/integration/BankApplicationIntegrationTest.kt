package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.BankApplication
import java.time.LocalDate
import java.time.LocalDateTime

class BankApplicationIntegrationTest : IntegrationTestBase() {

  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")
  private val fakeToday = LocalDate.parse("2023-08-17")

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
        BankApplication(applicationSubmittedDate = fakeToday, bankName = "Lloyds"),
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
        BankApplication(resubmissionDate = fakeToday, bankResponseDate = fakeToday, status = "Account opened"),
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
  }
}
