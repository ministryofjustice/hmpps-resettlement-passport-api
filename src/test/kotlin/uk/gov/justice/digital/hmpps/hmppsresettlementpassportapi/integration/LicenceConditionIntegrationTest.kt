package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.CvlApiMockServer.Companion.TEST_IMAGE_BASE64
import java.util.Base64

class LicenceConditionIntegrationTest : IntegrationTestBase() {
  @Test
  fun `Get image from licence condition happy path`() {
    val offenderId = "abc"
    val licenceId = "123"
    val conditionId = "456"
    val expectedOutput = Base64.getDecoder().decode(TEST_IMAGE_BASE64)

    cvlApiMockServer.stubGetImageFromLicenceIdAndConditionId(licenceId, conditionId, 200)
    webTestClient.get()
      .uri("/resettlement-passport/$offenderId/licence-condition/id/$licenceId/condition/$conditionId/image")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("image/jpeg")
      .expectBody<ByteArray>().consumeWith {
        Assertions.assertArrayEquals(expectedOutput, it.responseBody)
      }
  }

  @Test
  fun `Get image from licence condition not found`() {
    val offenderId = "abc"
    val licenceId = "123"
    val conditionId = "456"

    cvlApiMockServer.stubGetImageFromLicenceIdAndConditionId(licenceId, conditionId, 404)
    webTestClient.get()
      .uri("/resettlement-passport/$offenderId/licence-condition/id/$licenceId/condition/$conditionId/image")
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

  @Test
  fun `Get image from licence condition internal error`() {
    val offenderId = "abc"
    val licenceId = "123"
    val conditionId = "456"

    cvlApiMockServer.stubGetImageFromLicenceIdAndConditionId(licenceId, conditionId, 500)
    webTestClient.get()
      .uri("/resettlement-passport/$offenderId/licence-condition/id/$licenceId/condition/$conditionId/image")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage").isEqualTo("Unexpected error: 500 Internal Server Error from GET http://localhost:8095/exclusion-zone/id/123/condition/id/456/full-size-image")
      .jsonPath("developerMessage").isEqualTo("500 Internal Server Error from GET http://localhost:8095/exclusion-zone/id/123/condition/id/456/full-size-image")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  fun `Get image from licence unauthorized`() {
    val offenderId = "abc"
    val licenceId = "123"
    val conditionId = "456"

    // Failing to set a valid Authorization header should result in 401 response
    webTestClient.get()
      .uri("/resettlement-passport/$offenderId/licence-condition/id/$licenceId/condition/$conditionId/image")
      .exchange()
      .expectStatus().isEqualTo(401)
  }
}
