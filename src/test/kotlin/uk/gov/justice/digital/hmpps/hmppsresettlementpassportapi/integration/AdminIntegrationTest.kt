package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test

class AdminIntegrationTest : IntegrationTestBase() {

  @Test
  fun `POST send metrics - happy path`() {
    // Note - no auth as this endpoint is protected by ingress
    webTestClient.post()
      .uri("/send-metrics")
      .exchange()
      .expectStatus().isOk
  }
}
