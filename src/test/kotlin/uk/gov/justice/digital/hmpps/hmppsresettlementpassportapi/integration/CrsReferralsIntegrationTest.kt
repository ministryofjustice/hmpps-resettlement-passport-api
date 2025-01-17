package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.json.JsonCompareMode
import java.time.LocalDate
import java.time.Period

class CrsReferralsIntegrationTest : IntegrationTestBase() {
  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get All CRS Referrals happy path - with caching`() {
    var expectedOutput = readFile("testdata/expectation/crs-referrals.json")
    val dob = LocalDate.of(1982, 10, 24)
    val age = Period.between(dob, LocalDate.now()).years
    expectedOutput = expectedOutput.replace("REPLACE_WITH_AGE", "$age")
    val nomsId = "123"
    val crn = "abc"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    interventionsServiceApiMockServer.stubGet("/probation-case/$crn/referral", 200, "testdata/interventions-service-api/crs-referrals.json")
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, crn)
    deliusApiMockServer.stubGetComByCrn(crn, 200)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/crs-referrals")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)

    // Reset mocks to ensure it uses the cache
    prisonerSearchApiMockServer.resetAll()
    interventionsServiceApiMockServer.resetAll()
    deliusApiMockServer.resetAll()

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/crs-referrals")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  fun `Get CRS Referrals unauthorized`() {
    val nomsId = "G4274GN"
    // Failing to set a valid Authorization header should result in 401 response
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/crs-referrals")
      .exchange()
      .expectStatus().isEqualTo(401)
  }

  @Test
  fun `Get CRS Referrals forbidden`() {
    val nomsId = "G4274GN"
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/crs-referrals")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `Get CRS Referrals when nomsId not found`() {
    val nomsId = "123"
    val crn = "abc"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    interventionsServiceApiMockServer.stubGet("/probation-case/$crn/referral", 404, null)
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, crn)
    deliusApiMockServer.stubGetComByCrn(crn, 200)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/crs-referrals")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get Pathway Specific CRS Referrals happy path`() {
    var expectedOutput = readFile("testdata/expectation/crs-referrals-pathway-specific.json")
    val dob = LocalDate.of(1982, 10, 24)
    val age = Period.between(dob, LocalDate.now()).years
    expectedOutput = expectedOutput.replace("REPLACE_WITH_AGE", "$age")
    val nomsId = "123"
    val crn = "abc"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    interventionsServiceApiMockServer.stubGet("/probation-case/$crn/referral", 200, "testdata/interventions-service-api/crs-referrals.json")
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, crn)
    deliusApiMockServer.stubGetComByCrn(crn, 200)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/crs-referrals/ACCOMMODATION")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get Pathway Specific have No CRS Referrals happy path`() {
    var expectedOutput = readFile("testdata/expectation/crs-referrals-pathway-specific-empty.json")
    val dob = LocalDate.of(1982, 10, 24)
    val age = Period.between(dob, LocalDate.now()).years
    expectedOutput = expectedOutput.replace("REPLACE_WITH_AGE", "$age")
    val nomsId = "123"
    val crn = "abc"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    interventionsServiceApiMockServer.stubGet("/probation-case/$crn/referral", 200, "testdata/interventions-service-api/crs-referrals-no-matches.json")
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, crn)
    deliusApiMockServer.stubGetComByCrn(crn, 200)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/crs-referrals/HEALTH")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get CRS Referrals - 404 from interventions service api`() {
    var expectedOutput = readFile("testdata/expectation/crs-referrals-pathway-specific-empty.json")
    val dob = LocalDate.of(1982, 10, 24)
    val age = Period.between(dob, LocalDate.now()).years
    expectedOutput = expectedOutput.replace("REPLACE_WITH_AGE", "$age")
    val nomsId = "123"
    val crn = "abc"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    interventionsServiceApiMockServer.stubGet("/probation-case/$crn/referral", 404, null)
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, crn)
    deliusApiMockServer.stubGetComByCrn(crn, 200)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/crs-referrals/HEALTH")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }
}
