package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.google.common.io.Resources
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

class RisksIntegrationTest : IntegrationTestBase() {

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get risk scores happy path 1`() {
    val prisonerId = "123"
    val crn = "abc"
    val expectedOutput = Resources.getResource("testdata/arn/risk-scores.json").readText()

    communityApiMockServer.stubGetCrnFromNomsId(prisonerId, crn)
    arnApiMockServer.stubGetToCrn("/risks/crn/$crn/predictors/all", 200, "testdata/arn/crn-risk-predictors-1.json")

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
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get risk scores happy path 2 - multiple sets of risk predictors`() {
    val prisonerId = "123"
    val crn = "abc"
    val expectedOutput = Resources.getResource("testdata/arn/risk-scores.json").readText()

    communityApiMockServer.stubGetCrnFromNomsId(prisonerId, crn)
    arnApiMockServer.stubGetToCrn("/risks/crn/$crn/predictors/all", 200, "testdata/arn/crn-risk-predictors-2.json")

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
  fun `Get risk scores - no ARN found in database`() {
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
      .jsonPath("userMessage").isEqualTo("Resource not found. Check request parameters - Cannot find CRN for NomsId abc in database")
      .jsonPath("developerMessage").isEqualTo("Cannot find CRN for NomsId abc in database")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get risk scores - no data found in ARN API`() {
    val prisonerId = "123"
    val crn = "abc"

    communityApiMockServer.stubGetCrnFromNomsId(prisonerId, crn)
    arnApiMockServer.stubGetToCrn("/risks/crn/$crn/predictors/all", 404, null)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/risk/scores")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage").isEqualTo("Resource not found. Check request parameters - ARN service could not find CRN abc/NomsId 123")
      .jsonPath("developerMessage").isEqualTo("ARN service could not find CRN abc/NomsId 123")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get risk scores - internal server error`() {
    val prisonerId = "123"
    val crn = "abc"

    communityApiMockServer.stubGetCrnFromNomsId(prisonerId, crn)
    arnApiMockServer.stubGetToCrn("/risks/crn/$crn/predictors/all", 500, null)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/risk/scores")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage").isEqualTo("Unexpected error: 500 Internal Server Error from GET http://localhost:8097/risks/crn/abc/predictors/all")
      .jsonPath("developerMessage").isEqualTo("500 Internal Server Error from GET http://localhost:8097/risks/crn/abc/predictors/all")
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

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get RoSH happy path`() {
    val prisonerId = "123"
    val crn = "abc"
    val expectedOutput = Resources.getResource("testdata/arn/risk-rosh.json").readText()

    communityApiMockServer.stubGetCrnFromNomsId(prisonerId, crn)
    arnApiMockServer.stubGetToCrn("/risks/crn/$crn", 200, "testdata/arn/crn-risks.json")

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/risk/rosh")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  fun `Get RoSH - no ARN found in database`() {
    val prisonerId = "abc"

    communityApiMockServer.stubGetCrnFromNomsIdNotFound(prisonerId)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/risk/rosh")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage").isEqualTo("Resource not found. Check request parameters - Cannot find CRN for NomsId abc in database")
      .jsonPath("developerMessage").isEqualTo("Cannot find CRN for NomsId abc in database")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get RoSH - no data found in ARN API`() {
    val prisonerId = "123"
    val crn = "abc"

    communityApiMockServer.stubGetCrnFromNomsId(prisonerId, crn)
    arnApiMockServer.stubGetToCrn("/risks/crn/$crn", 404, null)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/risk/rosh")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage").isEqualTo("Resource not found. Check request parameters - ARN service could not find CRN abc/NomsId 123")
      .jsonPath("developerMessage").isEqualTo("ARN service could not find CRN abc/NomsId 123")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get RoSH scores - internal server error`() {
    val prisonerId = "123"
    val crn = "abc"

    communityApiMockServer.stubGetCrnFromNomsId(prisonerId, crn)
    arnApiMockServer.stubGetToCrn("/risks/crn/$crn", 500, null)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/risk/rosh")
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage").isEqualTo("Unexpected error: 500 Internal Server Error from GET http://localhost:8097/risks/crn/abc")
      .jsonPath("developerMessage").isEqualTo("500 Internal Server Error from GET http://localhost:8097/risks/crn/abc")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  fun `Get RoSH - unauthorized`() {
    val prisonerId = "abc"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/risk/rosh")
      .exchange()
      .expectStatus().isUnauthorized
  }
}
