package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test

class SecurityIntegrationTest : IntegrationTestBase() {
  @Test
  fun `Should receive 403 when call the retry-dlq queue admin endpoint without the correct role`() {
    webTestClient.put()
      .uri("/queue-admin/retry-all-dlqs")
      .headers(setAuthorisation(roles = listOf("SOMETHING_ELSE")))
      .exchange()
      .expectStatus()
      .isForbidden()
  }

  @Test
  fun `Should receive 401 when call the retry-dlq queue admin endpoint without authentication`() {
    webTestClient.put()
      .uri("/queue-admin/retry-all-dlqs")
      .exchange()
      .expectStatus()
      .isUnauthorized()
  }
}
