package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test

class EducationWorkSkillsIntegrationTest : IntegrationTestBase() {
  @Test
  fun `Get work readiness - happy path`() {
    val nomsId = "G4274GN"
    val expectedOutput = readFile("testdata/expectation/work-readiness.json")

    educationEmploymentApiMockServer.stubGet("/readiness-profiles/$nomsId", 200, "testdata/education-employment-api/readiness-profile.json")
    ciagApiMockServer.stubGet("/ciag/induction/$nomsId", 200, "testdata/ciag-api/ciag-induction.json")

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/work-readiness")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  fun `Get work readiness - not found`() {
    val nomsId = "G4274GN"
    val expectedOutput = readFile("testdata/expectation/work-readiness-no-data.json")

    educationEmploymentApiMockServer.stubGet("/readiness-profiles/$nomsId", 404, null)
    ciagApiMockServer.stubGet("/ciag/induction/$nomsId", 404, null)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/work-readiness")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  fun `Get work readiness - unauthorized`() {
    val nomsId = "abc"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/work-readiness")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Get work readiness - forbidden`() {
    val nomsId = "abc"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/work-readiness")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }
}