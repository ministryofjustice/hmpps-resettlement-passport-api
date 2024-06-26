package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

class AccommodationIntegrationTest : IntegrationTestBase() {
  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-2.sql")
  fun `Get Accommodation with noFixAbode true happy path - with caching`() {
    val expectedOutput = readFile("testdata/expectation/accommodation-1.json")
    val nomsId = "G1458GV"
    val crn = "CRN1"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, crn)
    deliusApiMockServer.stubGetAccommodationFromCRN(crn, true, 200)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/accommodation")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)

    // Reset mocks to ensure it uses the cache
    prisonerSearchApiMockServer.resetAll()
    deliusApiMockServer.resetAll()

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/accommodation")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-2.sql")
  fun `Get Accommodation with noFixAbode false happy path`() {
    val expectedOutput = readFile("testdata/expectation/accommodation-2.json")
    val nomsId = "G1458GV"
    val crn = "CRN1"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, crn)
    deliusApiMockServer.stubGetAccommodationFromCRN(crn, false, 200)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/accommodation")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)
  }

  @Test
  fun `Get Accommodation unauthorized`() {
    val nomsId = "G1458GV"
    // Failing to set a valid Authorization header should result in 401 response
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/accommodation")
      .exchange()
      .expectStatus().isEqualTo(401)
  }

  @Test
  fun `Get All Appointments forbidden`() {
    val nomsId = "G1458GV"
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/accommodation")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-2.sql")
  fun `Get All Appointments  Internal Error`() {
    val nomsId = "G1458GV"
    val crn = "CRN1"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, crn)
    deliusApiMockServer.stubGetAccommodationFromCRN(crn, false, 500)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/accommodation")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
  }

  @Test
  fun `Get Accommodation when nomsId not found`() {
    val nomsId = "G1458GV"
    val crn = "CRN1"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 404)
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, crn)
    deliusApiMockServer.stubGetAccommodationFromCRN(crn, true, 404)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/accommodation")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody().jsonPath("status").isEqualTo(404)
  }
}
