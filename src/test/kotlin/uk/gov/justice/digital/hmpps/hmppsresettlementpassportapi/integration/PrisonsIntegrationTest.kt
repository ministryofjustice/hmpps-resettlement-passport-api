package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test

class PrisonsIntegrationTest : IntegrationTestBase() {
  @Test
  fun `Get All Active Prisons happy path`() {
    val expectedJson = """
      [{"id": "SWI", "name": "Swansea", "active": true }]
    """.trimIndent()
    prisonRegisterApiMockServer.stubPrisonList(200)
    webTestClient.get()
      .uri("/resettlement-passport/prisons/active")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedJson)
  }

  @Test
  fun `Get All Active Prisons unauthorized`() {
    // Failing to set a valid Authorization header should result in 401 response
    webTestClient.get()
      .uri("/resettlement-passport/prisons/active")
      .exchange()
      .expectStatus().isEqualTo(401)
  }

  @Test
  fun `Get All Active Prisons  Internal Error`() {
    prisonRegisterApiMockServer.stubPrisonList(500)
    webTestClient.get()
      .uri("/resettlement-passport/prisons/active")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
  }

  @Test
  fun `Get All Prisons happy path`() {
    val expectedJson = """
      [{"id": "AKI", "name": "Acklington", "active": false }, {"id": "SWI", "name": "Swansea", "active": true }]
    """.trimIndent()
    prisonRegisterApiMockServer.stubPrisonList(200)
    webTestClient.get()
      .uri("/resettlement-passport/prisons/all")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedJson, true)
  }

  @Test
  fun `Get All Prisons unauthorized`() {
    // Failing to set a valid Authorization header should result in 401 response
    webTestClient.get()
      .uri("/resettlement-passport/prisons/all")
      .exchange()
      .expectStatus().isEqualTo(401)
  }

  @Test
  fun `Get All Prisons  Internal Error`() {
    prisonRegisterApiMockServer.stubPrisonList(500)
    webTestClient.get()
      .uri("/resettlement-passport/prisons/all")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
  }
}
