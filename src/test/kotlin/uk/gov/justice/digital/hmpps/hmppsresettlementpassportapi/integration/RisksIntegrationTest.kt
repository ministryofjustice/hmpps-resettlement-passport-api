package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

class RisksIntegrationTest : IntegrationTestBase() {

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get risk scores happy path 1`() {
    val nomsId = "123"
    val crn = "abc"
    val expectedOutput = readFile("testdata/expectation/risk-scores.json")

    arnApiMockServer.stubGetToCrn("/risks/crn/$crn/predictors/all", 200, "testdata/arn-api/crn-risk-predictors-1.json")

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/risk/scores")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get risk scores happy path 2 - multiple sets of risk predictors`() {
    val nomsId = "123"
    val crn = "abc"
    val expectedOutput = readFile("testdata/expectation/risk-scores.json")

    arnApiMockServer.stubGetToCrn("/risks/crn/$crn/predictors/all", 200, "testdata/arn-api/crn-risk-predictors-2.json")

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/risk/scores")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  fun `Get risk scores - no ARN found in database`() {
    val nomsId = "abc"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/risk/scores")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage")
      .isEqualTo("Resource not found. Check request parameters - Cannot find CRN for NomsId abc in database")
      .jsonPath("developerMessage").isEqualTo("Cannot find CRN for NomsId abc in database")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get risk scores - no data found in ARN API`() {
    val nomsId = "123"
    val crn = "abc"

    arnApiMockServer.stubGetToCrn("/risks/crn/$crn/predictors/all", 404, null)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/risk/scores")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage")
      .isEqualTo("Resource not found. Check request parameters - ARN service could not find CRN abc")
      .jsonPath("developerMessage").isEqualTo("ARN service could not find CRN abc")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get risk scores - internal server error`() {
    val nomsId = "123"
    val crn = "abc"

    arnApiMockServer.stubGetToCrn("/risks/crn/$crn/predictors/all", 500, null)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/risk/scores")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage")
      .isEqualTo("Unexpected error: 500 Internal Server Error from GET http://localhost:8097/risks/crn/abc/predictors/all")
      .jsonPath("developerMessage")
      .isEqualTo("500 Internal Server Error from GET http://localhost:8097/risks/crn/abc/predictors/all")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  fun `Get risk scores - unauthorized`() {
    val nomsId = "abc"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/risk/scores")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Get risk scores - forbidden`() {
    val nomsId = "abc"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/risk/scores")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get RoSH happy path`() {
    val nomsId = "123"
    val crn = "abc"
    val expectedOutput = readFile("testdata/expectation/risk-rosh.json")

    arnApiMockServer.stubGetToCrn("/risks/crn/$crn", 200, "testdata/arn-api/crn-risks.json")

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/risk/rosh")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  fun `Get RoSH - no ARN found in database`() {
    val nomsId = "abc"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/risk/rosh")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage")
      .isEqualTo("Resource not found. Check request parameters - Cannot find CRN for NomsId abc in database")
      .jsonPath("developerMessage").isEqualTo("Cannot find CRN for NomsId abc in database")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get RoSH - no data found in ARN API`() {
    val nomsId = "123"
    val crn = "abc"

    arnApiMockServer.stubGetToCrn("/risks/crn/$crn", 404, null)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/risk/rosh")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage")
      .isEqualTo("Resource not found. Check request parameters - ARN service could not find CRN abc")
      .jsonPath("developerMessage").isEqualTo("ARN service could not find CRN abc")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get RoSH scores - internal server error`() {
    val nomsId = "123"
    val crn = "abc"

    arnApiMockServer.stubGetToCrn("/risks/crn/$crn", 500, null)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/risk/rosh")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage")
      .isEqualTo("Unexpected error: 500 Internal Server Error from GET http://localhost:8097/risks/crn/abc")
      .jsonPath("developerMessage").isEqualTo("500 Internal Server Error from GET http://localhost:8097/risks/crn/abc")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  fun `Get RoSH - unauthorized`() {
    val nomsId = "abc"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/risk/rosh")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Get RoSH - forbidden`() {
    val nomsId = "abc"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/risk/rosh")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get MAPPA happy path`() {
    val nomsId = "123"
    val crn = "abc"
    val expectedOutput = readFile("testdata/expectation/risk-mappa.json")

    deliusApiMockServer.stubGetToCrn(
      "/probation-cases/$crn/mappa",
      200,
      "testdata/resettlement-passport-delius-api/delius-risk-mappa.json",
    )

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/risk/mappa")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  fun `Get MAPPA - no ARN found in database`() {
    val nomsId = "abc"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/risk/mappa")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage")
      .isEqualTo("Resource not found. Check request parameters - Cannot find CRN for NomsId abc in database")
      .jsonPath("developerMessage").isEqualTo("Cannot find CRN for NomsId abc in database")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get MAPPA - no data found in Delius API`() {
    val nomsId = "123"
    val crn = "abc"

    deliusApiMockServer.stubGetToCrn("/probation-cases/$crn/mappa", 404, null)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/risk/mappa")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage")
      .isEqualTo("Resource not found. Check request parameters - Cannot find MAPPA Data for NomsId 123 / CRN abc in Delius API")
      .jsonPath("developerMessage").isEqualTo("Cannot find MAPPA Data for NomsId 123 / CRN abc in Delius API")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get MAPPA scores - internal server error`() {
    val nomsId = "123"
    val crn = "abc"

    deliusApiMockServer.stubGetToCrn("/probation-cases/$crn/mappa", 500, null)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/risk/mappa")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
      .jsonPath("errorCode").isEmpty
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  fun `Get MAPPA - unauthorized`() {
    val nomsId = "abc"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/risk/mappa")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Get MAPPA - forbidden`() {
    val nomsId = "abc"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/risk/mappa")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }
}
