package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.CvlApiMockServer.Companion.TEST_IMAGE_BASE64
import java.util.Base64

class LicenceConditionIntegrationTest : IntegrationTestBase() {

  @Test
  fun `Get image from licence condition happy path`() {
    val nomsId = "abc"
    val licenceId = "123"
    val conditionId = "456"
    val expectedOutput = Base64.getDecoder().decode(TEST_IMAGE_BASE64)

    cvlApiMockServer.stubGetImageFromLicenceIdAndConditionId(licenceId, conditionId, 200)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/licence-condition/id/$licenceId/condition/$conditionId/image")
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
    val nomsId = "abc"
    val licenceId = "123"
    val conditionId = "456"

    cvlApiMockServer.stubGetImageFromLicenceIdAndConditionId(licenceId, conditionId, 404)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/licence-condition/id/$licenceId/condition/$conditionId/image")
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
    val nomsId = "abc"
    val licenceId = "123"
    val conditionId = "456"

    cvlApiMockServer.stubGetImageFromLicenceIdAndConditionId(licenceId, conditionId, 500)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/licence-condition/id/$licenceId/condition/$conditionId/image")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage").isEqualTo("Unexpected error: 500 Internal Server Error from GET http://localhost:9095/exclusion-zone/id/123/condition/id/456/full-size-image")
      .jsonPath("developerMessage").isEqualTo("500 Internal Server Error from GET http://localhost:9095/exclusion-zone/id/123/condition/id/456/full-size-image")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  fun `Get image from licence unauthorized`() {
    val nomsId = "abc"
    val licenceId = "123"
    val conditionId = "456"

    // Failing to set a valid Authorization header should result in 401 response
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/licence-condition/id/$licenceId/condition/$conditionId/image")
      .exchange()
      .expectStatus().isEqualTo(401)
  }

  @Test
  fun `Get image from licence forbidden`() {
    val nomsId = "abc"
    val licenceId = "123"
    val conditionId = "456"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/licence-condition/id/$licenceId/condition/$conditionId/image")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp.sql")
  fun `Get licence condition from cvl happy path`() {
    val nomsId = "G4161UF"
    val expectedOutput = readFile("testdata/expectation/licence-condition.json")
    val licenceId = 101
    cvlApiMockServer.stubFindLicencesByNomsId(nomsId, 200)
    cvlApiMockServer.stubFetchLicenceConditionsByLicenceId(licenceId, 200)
    getLicenceConditions(nomsId)
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp.sql")
  fun `Get licence condition from cvl happy path with no active but dateCreated latest`() {
    val nomsId = "G4161UF"
    val expectedOutput = readFile("testdata/expectation/licence-condition.json")
    expectedOutput.replace("Active", "InActive", true)
    val licenceId = 101
    cvlApiMockServer.stubFindLicencesByNomsId(nomsId, 200)
    cvlApiMockServer.stubFetchLicenceConditionsByLicenceId(licenceId, 200)
    getLicenceConditions(nomsId)
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp.sql")
  fun `Get licence condition with includeChangeNotify should show change status is true the first time`() {
    val nomsId = "G4161UF"
    val expectedOutput = readFile("testdata/expectation/licence-condition-1.json")
    expectedOutput.replace("Active", "InActive", true)
    val licenceId = 101
    cvlApiMockServer.stubFindLicencesByNomsId(nomsId, 200)
    cvlApiMockServer.stubFetchLicenceConditionsByLicenceId(licenceId, 200)
    getConditionsWithChangeNotify(nomsId)
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)
  }

  @Test
  fun `Get licence condition from cvl unauthorized`() {
    val nomsId = "abc"

    // Failing to set a valid Authorization header should result in 401 response
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/licence-condition")
      .exchange()
      .expectStatus().isEqualTo(401)
  }

  @Test
  fun `Get licence condition from cvl forbidden`() {
    val nomsId = "abc"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/licence-condition")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `Get licence condition from cvl Internal Error`() {
    val nomsId = "abc"
    val licenceId = 101

    cvlApiMockServer.stubFindLicencesByNomsId(nomsId, 500)
    cvlApiMockServer.stubFetchLicenceConditionsByLicenceId(licenceId, 500)
    getLicenceConditions(nomsId)
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
  }

  @Test
  fun `Get licence condition from cvl when nomsId not found`() {
    val nomsId = "abc"

    cvlApiMockServer.stubFindLicencesByNomsId(nomsId, responseBody = "[]")
    getLicenceConditions(nomsId)
      .expectStatus().isEqualTo(404)
      .expectBody()
      .jsonPath("status").isEqualTo(404)
  }

  private fun getLicenceConditions(nomsId: String): WebTestClient.ResponseSpec =
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/licence-condition")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectHeader().contentType("application/json")

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp.sql")
  fun `Get licence condition from cvl happy path with licence condition data no change`() {
    val nomsId = "G4161UF"
    val expectedOutput = readFile("testdata/expectation/licence-condition.json")
    val licenceId = 101
    cvlApiMockServer.stubFindLicencesByNomsId(nomsId, 200)
    cvlApiMockServer.stubFetchLicenceConditionsByLicenceId(licenceId, 200)
    getConditionsWithChangeNotify(nomsId)
      .expectStatus().isOk
      .expectBody().json(expectedOutput)
      .jsonPath("$.changeStatus").isEqualTo(true)

    getConditionsWithChangeNotify(nomsId)
      .expectStatus().isOk
      .expectBody().json(expectedOutput)
      .jsonPath("$.changeStatus").isEqualTo(false)
      .jsonPath("$.version").isEqualTo(1)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp.sql")
  fun `Get licence condition with change will update the changeStatus flag`() {
    val nomsId = "G4161UF"
    val licenceId = 101
    cvlApiMockServer.stubFindLicencesByNomsId(nomsId, 200)
    cvlApiMockServer.stubFetchLicenceConditionsByLicenceId(licenceId, 200)
    getConditionsWithChangeNotify(nomsId)
      .expectStatus().isOk
      .expectBody().json(readFile("testdata/expectation/licence-condition.json"))
      .jsonPath("$.changeStatus").isEqualTo(true)

    cvlApiMockServer.stubFetchLicenceConditionsByLicenceId(licenceId, 200, readFile("testdata/cvl-api/licence-changed.json"))

    getConditionsWithChangeNotify(nomsId)
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.changeStatus").isEqualTo(true)
      .jsonPath("$.version").isEqualTo(2)
      .jsonPath("$.otherLicenseConditions[?(@.id == 1009)].text")
      .value { conditionText: List<String> ->
        assertThat(conditionText.firstOrNull()).contains("Report to staff at Rasasa at 04:01 am")
      }
  }

  private fun getConditionsWithChangeNotify(@Suppress("SameParameterValue") nomsId: String): WebTestClient.ResponseSpec =
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/licence-condition?includeChangeNotify=true")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectHeader().contentType("application/json")
}
