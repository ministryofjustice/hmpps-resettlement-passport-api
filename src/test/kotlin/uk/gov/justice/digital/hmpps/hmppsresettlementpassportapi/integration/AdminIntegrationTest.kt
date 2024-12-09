package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test

class AdminIntegrationTest : IntegrationTestBase() {
  @Test
  fun `PUT retry failed Delius case notes - happy path`() {
    // Note - no auth as this endpoint is protected by ingress
    webTestClient.put()
      .uri("/retry-failed-delius-case-notes")
      .exchange()
      .expectStatus().isOk
  }

  @Test
  fun `POST send metrics - happy path`() {
    // Note - no auth as this endpoint is protected by ingress
    webTestClient.post()
      .uri("/send-metrics")
      .exchange()
      .expectStatus().isOk
  }
}
