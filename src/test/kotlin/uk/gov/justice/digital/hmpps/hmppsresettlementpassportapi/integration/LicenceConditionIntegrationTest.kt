package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.CvlApiMockServer.Companion.TEST_IMAGE_BASE64
import java.util.Base64

class LicenceConditionIntegrationTest : IntegrationTestBase() {
  @Test
  fun `Get image from licence condition happy path`() {
    val prisonerId = "abc"
    val licenceId = "123"
    val conditionId = "456"
    val expectedOutput = Base64.getDecoder().decode(TEST_IMAGE_BASE64)

    cvlApiMockServer.stubGetImageFromLicenceIdAndConditionId(licenceId, conditionId, 200)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/licence-condition/id/$licenceId/condition/$conditionId/image")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("image/jpeg")
      .expectBody<ByteArray>().consumeWith {
        Assertions.assertArrayEquals(expectedOutput, it.responseBody)
      }
  }

  @Test
  fun `Get image from licence condition not found`() {
    val prisonerId = "abc"
    val licenceId = "123"
    val conditionId = "456"

    cvlApiMockServer.stubGetImageFromLicenceIdAndConditionId(licenceId, conditionId, 404)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/licence-condition/id/$licenceId/condition/$conditionId/image")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
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
    val prisonerId = "abc"
    val licenceId = "123"
    val conditionId = "456"

    cvlApiMockServer.stubGetImageFromLicenceIdAndConditionId(licenceId, conditionId, 500)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/licence-condition/id/$licenceId/condition/$conditionId/image")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
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
    val prisonerId = "abc"
    val licenceId = "123"
    val conditionId = "456"

    // Failing to set a valid Authorization header should result in 401 response
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/licence-condition/id/$licenceId/condition/$conditionId/image")
      .exchange()
      .expectStatus().isEqualTo(401)
  }

  @Test
  fun `Get image from licence forbidden`() {
    val prisonerId = "abc"
    val licenceId = "123"
    val conditionId = "456"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/licence-condition/id/$licenceId/condition/$conditionId/image")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `Get licence condition from cvl happy path`() {
    val prisonerId = "abc"
    val expectedOutput = readFile("testdata/expectation/licence-condition.json")
    val licenceId = 101
    cvlApiMockServer.stubFindLicencesByNomsId(prisonerId, 200)
    cvlApiMockServer.stubFetchLicenceConditionsByLicenceId(licenceId, 200)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/licence-condition")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)
  }

  @Test
  fun `Get licence condition from cvl happy path with no active but dateCreated latest`() {
    val prisonerId = "abc"
    val expectedOutput = readFile("testdata/expectation/licence-condition.json")
    expectedOutput.replace("Active", "InActive", true)
    val licenceId = 101
    cvlApiMockServer.stubFindLicencesByNomsId(prisonerId, 200)
    cvlApiMockServer.stubFetchLicenceConditionsByLicenceId(licenceId, 200)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/licence-condition")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)
  }

  @Test
  fun `Get licence condition from cvl unauthorized`() {
    val prisonerId = "abc"

    // Failing to set a valid Authorization header should result in 401 response
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/licence-condition")
      .exchange()
      .expectStatus().isEqualTo(401)
  }

  @Test
  fun `Get licence condition from cvl forbidden`() {
    val prisonerId = "abc"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/licence-condition")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `Get licence condition from cvl Internal Error`() {
    val prisonerId = "abc"
    val licenceId = 101

    cvlApiMockServer.stubFindLicencesByNomsId(prisonerId, 500)
    cvlApiMockServer.stubFetchLicenceConditionsByLicenceId(licenceId, 500)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/licence-condition")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
  }

  @Test
  fun `Get licence condition from cvl when prisonerId not found`() {
    val prisonerId = "abc"

    cvlApiMockServer.stubFindLicencesByNomsId(prisonerId, 404)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/licence-condition")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
  }
}
