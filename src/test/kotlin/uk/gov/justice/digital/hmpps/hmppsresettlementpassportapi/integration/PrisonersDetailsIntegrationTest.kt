package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.CvlApiMockServer
import java.io.File
import java.time.LocalDate
import java.time.Period
import java.util.*

class PrisonersDetailsIntegrationTest : IntegrationTestBase() {
  @Test
  fun `Get Prisoner Details happy path`() {
    val expectedOutput = File("src/test/resources/testdata/prisoners/prisoner-details.json").inputStream().readBytes()
      .toString(Charsets.UTF_8)
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

  @Test
  fun `Get image for Prisoner happy path`() {
    val nomisId = "abc"
    val imageId = "1313058"
    val expectedOutput = Base64.getDecoder().decode(CvlApiMockServer.TEST_IMAGE_BASE64)

    prisonApiMockServer.stubGetPrisonerImages(nomisId, 200)
    prisonApiMockServer.stubGetPrisonerFacialImage(imageId, 200)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomisId/image/$imageId")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("image/jpeg")
      .expectBody<ByteArray>().consumeWith {
        Assertions.assertArrayEquals(expectedOutput, it.responseBody)
      }
  }

  @Test
  fun `Get Prisoner image not found`() {
    val nomisId = "abc"
    val imageId = "1313058"
    val expectedOutput = Base64.getDecoder().decode(CvlApiMockServer.TEST_IMAGE_BASE64)

    prisonApiMockServer.stubGetPrisonerImages(nomisId, 200)
    prisonApiMockServer.stubGetPrisonerFacialImage(imageId, 404)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomisId/image/$imageId")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage").isEqualTo("Resource not found. Check request parameters - Image not found")
      .jsonPath("developerMessage").isEqualTo("Image not found")
      .jsonPath("moreInfo").isEmpty
  }
}
