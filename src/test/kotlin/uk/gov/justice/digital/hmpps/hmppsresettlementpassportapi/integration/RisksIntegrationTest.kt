package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test
import wiremock.com.google.common.io.Resources

class RisksIntegrationTest : IntegrationTestBase() {

  @Test
  fun `Get risk scores happy path 1`() {
    val prisonerId = "abc"
    val crn = "def"
    val expectedOutput = Resources.getResource("testdata/arn/risk-scores.json").readText()

    communityApiMockServer.stubGetCrnFromNomsId(prisonerId, crn)
    arnApiMockServer.stubGetRisksPredictorsFromCrn(crn, 200, "testdata/arn/crn-risk-predictors-1.json")

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/risk/scores")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  fun `Get risk scores happy path 2 - multiple sets of risk predictors`() {
    val prisonerId = "abc"
    val crn = "def"
    val expectedOutput = Resources.getResource("testdata/arn/risk-scores.json").readText()

    communityApiMockServer.stubGetCrnFromNomsId(prisonerId, crn)
    arnApiMockServer.stubGetRisksPredictorsFromCrn(crn, 200, "testdata/arn/crn-risk-predictors-2.json")

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/risk/scores")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  fun `Get risk scores - no ARN found in community API`() {
    val prisonerId = "abc"

    communityApiMockServer.stubGetCrnFromNomsIdNotFound(prisonerId)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/risk/scores")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage").isEqualTo("Resource not found. Check request parameters - Cannot find CRN for NomsId abc in Community API")
      .jsonPath("developerMessage").isEqualTo("Cannot find CRN for NomsId abc in Community API")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  fun `Get risk scores - no data found in ARN API`() {
    val prisonerId = "abc"
    val crn = "def"

    communityApiMockServer.stubGetCrnFromNomsId(prisonerId, crn)
    arnApiMockServer.stubGetRisksPredictorsFromCrn(crn, 404, null)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/risk/scores")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage").isEqualTo("Resource not found. Check request parameters - ARN service could not find CRN def/NomsId abc")
      .jsonPath("developerMessage").isEqualTo("ARN service could not find CRN def/NomsId abc")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  fun `Get risk scores - internal server error`() {
    val prisonerId = "abc"
    val crn = "def"

    communityApiMockServer.stubGetCrnFromNomsId(prisonerId, crn)
    arnApiMockServer.stubGetRisksPredictorsFromCrn(crn, 500, null)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/risk/scores")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage").isEqualTo("Unexpected error: 500 Internal Server Error from GET http://localhost:8097/risks/crn/def/predictors/all")
      .jsonPath("developerMessage").isEqualTo("500 Internal Server Error from GET http://localhost:8097/risks/crn/def/predictors/all")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  fun `Get risk scores - unauthorized`() {
    val prisonerId = "abc"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/risk/scores")
      .exchange()
      .expectStatus().isUnauthorized
  }
}
