package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.BankApplication
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.SupportNeedsIntegrationTest.Companion.fakeNow

class BankApplicationIntegrationReadOnlyModeTest : ReadOnlyIntegrationTestBase() {

  @Test
  @Sql("classpath:testdata/sql/seed-bank-application.sql")
  fun `Create bank application - forbidden`() {
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/123/bankapplication")
      .bodyValue(
        BankApplication(applicationSubmittedDate = fakeNow, bankName = "Lloyds"),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-bank-application.sql")
  fun `Update bank application - forbidden`() {
    webTestClient.patch()
      .uri("/resettlement-passport/prisoner/123/bankapplication/1")
      .bodyValue(
        BankApplication(resubmissionDate = fakeNow, bankResponseDate = fakeNow, status = "Account opened"),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-bank-application.sql")
  fun `Delete bank application - forbidden`() {
    webTestClient.delete()
      .uri("/resettlement-passport/prisoner/123/bankapplication/1")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isForbidden
  }
}
