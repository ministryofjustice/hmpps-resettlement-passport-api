package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

class AppointmentsIntegrationTest : IntegrationTestBase() {
  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-2.sql")
  fun `Get All Appointments happy path`() {
    val expectedOutput = readFile("testdata/expectation/appointments.json")
    val nomisId = "G1458GV"
    val crn = "CRN1"
    deliusApiMockServer.stubGetCrnFromNomsId(nomisId, crn)
    deliusApiMockServer.stubGetAppointmentsFromCRN(crn, 200)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomisId/appointments?page=0&size=50")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)
  }

  @Test
  fun `Get All Appointments unauthorized`() {
    val nomisId = "G1458GV"
    // Failing to set a valid Authorization header should result in 401 response
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomisId/appointments?page=0&size=50")
      .exchange()
      .expectStatus().isEqualTo(401)
  }

  @Test
  fun `Get All Appointments forbidden`() {
    val nomisId = "G1458GV"
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomisId/appointments?page=0&size=50")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-2.sql")
  fun `Get All Appointments  Internal Error`() {
    val nomisId = "G1458GV"
    val crn = "CRN1"
    deliusApiMockServer.stubGetCrnFromNomsId(nomisId, crn)
    deliusApiMockServer.stubGetAppointmentsFromCRN(crn, 500)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomisId/appointments?page=0&size=50")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
  }

  @Test
  fun `Get All Appointments  negative Page number`() {
    val nomisId = "G1458GV"
    val crn = "CRN1"
    deliusApiMockServer.stubGetAppointmentsFromCRN(crn, 404)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomisId/appointments?page=-1&size=50")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("developerMessage").toString().contains("No Data found")
  }

  @Test
  fun `Get All Prisoners  negative Page size`() {
    val nomisId = "G1458GV"
    val crn = "CRN1"
    deliusApiMockServer.stubGetAppointmentsFromCRN(crn, 404)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomisId/appointments?page=0&size=-50")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("developerMessage").toString().contains("No Data found")
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-2.sql")
  fun `Get All Prisoners with no page and no size as Internal Error`() {
    val nomisId = "G1458GV"
    val crn = "CRN1"
    deliusApiMockServer.stubGetAppointmentsFromCRN(crn, 500)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomisId/appointments?page=0&size=50")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
  }

  @Test
  fun `Get Appointments when prisonerId not found`() {
    val prisonId = "abc"

    offenderSearchApiMockServer.stubGetPrisonersList(prisonId, "", 500, 0, 404)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?page=0&size=10&sort=xxxxx")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
  }
}
