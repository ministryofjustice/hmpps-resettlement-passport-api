package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDate
import java.time.Period

class PrisonersDetailsIntegrationTest : IntegrationTestBase() {
  @Test
  fun `Get Prisoner Details happy path`() {
    val expectedOutput = File("src/test/resources/testdata/prisoners/prisoner-details.json").inputStream().readBytes().toString(Charsets.UTF_8)
    val dob = LocalDate.of(1982, 10, 24)
    val age = Period.between(dob, LocalDate.now()).years
    expectedOutput.replace("40,", "$age,")
    val nomisId = "G4274GN"
    offenderSearchApiMockServer.stubGetPrisonerDetails(nomisId, 200)
    prisonApiMockServer.stubGetPrisonerImages(nomisId, 200)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomisId")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)
  }

  @Test
  fun `Get Prisoner Details unauthorized`() {
    val nomisId = "G4274GN"
    // Failing to set a valid Authorization header should result in 401 response
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomisId")
      .exchange()
      .expectStatus().isEqualTo(401)
  }

  @Test
  fun `Get Prisoner Details when nomisId not found`() {
    val nomisId = "abc"

    offenderSearchApiMockServer.stubGetPrisonerDetails(nomisId, 404)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomisId")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
  }
}
