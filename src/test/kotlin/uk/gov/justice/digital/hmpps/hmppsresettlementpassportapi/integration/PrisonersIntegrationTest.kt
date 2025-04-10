package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.json.JsonCompareMode

class PrisonersIntegrationTest : IntegrationTestBase() {
  @Test
  @Sql("classpath:testdata/sql/seed-prisoners-happy-path.sql")
  fun `Get All Prisoners happy path - with caching`() {
    val expectedOutput = readFile("testdata/expectation/prisoners-happy-path.json")
    val prisonId = "MDI"
    prisonerSearchApiMockServer.stubGetPrisonersList(prisonId, 500, 0, "", 200)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?term=&page=0&size=10&sort=releaseDate,DESC")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)

    // Reset mocks to ensure it uses the cache
    prisonerSearchApiMockServer.resetAll()

    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?term=&page=0&size=10&sort=releaseDate,DESC")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-9.sql")
  fun `Get All Prisoners happy path - including past release dates`() {
    val expectedOutput = readFile("testdata/expectation/prisoners-include-past-release-dates.json")
    val prisonId = "MDI"
    prisonerSearchApiMockServer.stubGetPrisonersList(prisonId, 500, 0, "", 200)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?term=&page=0&size=10&sort=releaseDate,DESC&includePastReleaseDates=true")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-9.sql")
  fun `Get All Prisoners - check caching, with and without a search term gets different results`() {
    val prisonId = "MDI"

    // Stub get prisoners to include Finn Chandlevieve
    prisonerSearchApiMockServer.stubGetPrisonersList(prisonId, 500, 0, "", 200)

    // Call GET prisoners with no search term and assert json
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?term=&page=0&size=10&sort=releaseDate,DESC")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(readFile("testdata/expectation/prisoners.json"), JsonCompareMode.STRICT)

    // Update test stub to filter on Finn Chandlevieve
    prisonerSearchApiMockServer.stubGetPrisonersList("testdata/prisoner-search-api/prisoner-search-filtered-1.json", prisonId, 500, 0, "Finn", 200)

    // Call GET prisoners with search term and assert json (should not use cache because the search term is different,
    // so returns the filtered results)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?term=Finn&page=0&size=10&sort=releaseDate,DESC")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(readFile("testdata/expectation/prisoners-with-term.json"), JsonCompareMode.STRICT)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-9.sql")
  fun `Get All Prisoners sort by releaseDate ascending happy path`() {
    val expectedOutput = readFile("testdata/expectation/prisoners-ascending.json")
    val prisonId = "MDI"
    prisonerSearchApiMockServer.stubGetPrisonersList(prisonId, 500, 0, "", 200)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?term=&page=0&size=10&sort=releaseDate,ASC")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  fun `Get All Prisoners unauthorized`() {
    val prisonId = "MDI"
    // Failing to set a valid Authorization header should result in 401 response
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners")
      .exchange()
      .expectStatus().isEqualTo(401)
  }

  @Test
  fun `Get All Prisoners forbidden`() {
    val prisonId = "MDI"
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?term=&page=0&size=10&sort=releaseDate,ASC")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `Get All Prisoners  Internal Error`() {
    val prisonId = "MDI"
    prisonerSearchApiMockServer.stubGetPrisonersList(prisonId, 500, 0, "", 500)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
  }

  @Test
  fun `Get All Prisoners  negative Page number`() {
    val prisonId = "MDI"
    prisonerSearchApiMockServer.stubGetPrisonersList(prisonId, 500, 0, "", 404)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?page=-1&size=10&sort=releaseDate,ASC")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("developerMessage").toString().contains("No Data found")
  }

  @Test
  fun `Get All Prisoners  negative Page size`() {
    val prisonId = "MDI"
    prisonerSearchApiMockServer.stubGetPrisonersList(prisonId, 500, 0, "", 404)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?page=1&size=-1&sort=releaseDate,ASC")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("developerMessage").toString().contains("No Data found")
  }

  @Test
  fun `Get All Prisoners with no page and no size as Internal Error`() {
    val prisonId = "MDI"
    prisonerSearchApiMockServer.stubGetPrisonersList(prisonId, 500, 0, "", 500)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?sort=releaseDate,ASC")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
  }

  @Test
  fun `Get All Prisoners with no sort as Internal Error`() {
    val prisonId = "MDI"
    prisonerSearchApiMockServer.stubGetPrisonersList(prisonId, 500, 0, "", 500)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?page=1&sie=10")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(500)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(500)
  }

  @Test
  fun `Get All Prisoners with sort invalid`() {
    val prisonId = "MDI"
    prisonerSearchApiMockServer.stubGetPrisonersList(prisonId, 500, 0, "", 200)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?page=0&size=10&sort=xxxxx")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("developerMessage").toString().contains("No Data found")
  }

  @Test
  fun `Get All Prisoners with sort by pathway status- no pathway view selected`() {
    val prisonId = "MDI"
    prisonerSearchApiMockServer.stubGetPrisonersList(prisonId, 500, 0, "", 200)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?page=0&size=10&sort=pathwayStatus,ASC")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(400)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(400)
      .jsonPath("developerMessage").toString().contains("Pathway must be selected to sort by pathway status")
  }

  @Test
  fun `Get Prisoners when nomsId not found`() {
    val prisonId = "abc"

    prisonerSearchApiMockServer.stubGetPrisonersList(prisonId, 500, 0, "", 404)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?page=0&size=10&sort=xxxxx")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
  }

  @Test
  fun `Get All Prisoners - 400 pathway does not exist`() {
    val prisonId = "MDI"
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?term=&page=0&size=10&sort=releaseDate,DESC&pathwayView=FAKE_PATHWAY")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(400)
  }

  @Test
  fun `Get All Prisoners - 400 status does not exist`() {
    val prisonId = "MDI"
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?term=&page=0&size=10&sort=releaseDate,DESC&pathwayView=ACCOMMODATION&pathwayStatus=FAKE_STATUS")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(400)
  }

  @Test
  fun `Get All Prisoners - 400 pathwayStatus given with no pathwayView`() {
    val prisonId = "MDI"
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?term=&page=0&size=10&sort=releaseDate,DESC&pathwayStatus=NOT_STARTED")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(400)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-2.sql")
  fun `Get All Prisoners happy path - volume test`() {
    val prisonId = "MDI"
    prisonerSearchApiMockServer.stubGetPrisonersList("testdata/prisoner-search-api/prisoner-search-volume-test-1.json", prisonId, 500, 0, "", 200)
    prisonerSearchApiMockServer.stubGetPrisonersList("testdata/prisoner-search-api/prisoner-search-volume-test-2.json", prisonId, 500, 1, "", 200)
    prisonerSearchApiMockServer.stubGetPrisonersList("testdata/prisoner-search-api/prisoner-search-volume-test-3.json", prisonId, 500, 2, "", 200)
    prisonerSearchApiMockServer.stubGetPrisonersList("testdata/prisoner-search-api/prisoner-search-volume-test-4.json", prisonId, 500, 3, "", 200)
    prisonerSearchApiMockServer.stubGetPrisonersList("testdata/prisoner-search-api/prisoner-search-volume-test-5.json", prisonId, 500, 4, "", 200)
    prisonerSearchApiMockServer.stubGetPrisonersList("testdata/prisoner-search-api/prisoner-search-volume-test-6.json", prisonId, 500, 5, "", 200)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?term=&page=0&size=10&sort=releaseDate,DESC")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
  }

  @Test
  fun `Get All Prisoners - 400 pathwayView given with assessmentRequired`() {
    val prisonId = "MDI"
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?term=&page=0&size=10&sort=releaseDate,DESC&pathwayView=ACCOMMODATION&assessmentRequired=true")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(400)
  }

  @Test
  fun `Get All Prisoners - 400 assessmentRequired not a boolean`() {
    val prisonId = "MDI"
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?term=&page=0&size=10&sort=releaseDate,DESC&assessmentRequired=string")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(400)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-9.sql")
  fun `Get All Prisoners happy path - assessmentRequired true`() {
    val expectedOutput = readFile("testdata/expectation/prisoners-assessment-required.json")
    val prisonId = "MDI"
    prisonerSearchApiMockServer.stubGetPrisonersList(prisonId, 500, 0, "", 200)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?term=&assessmentRequired=true&page=0&size=10&sort=releaseDate,DESC")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-9.sql")
  fun `Get All Prisoners happy path - assessmentRequired false`() {
    val expectedOutput = readFile("testdata/expectation/prisoners-assessment-not-required.json")
    val prisonId = "MDI"
    prisonerSearchApiMockServer.stubGetPrisonersList(prisonId, 500, 0, "", 200)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?term=&assessmentRequired=false&page=0&size=10&sort=releaseDate,DESC")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-case-allocation.sql")
  fun `Get All Prisoners happy path - with assignedworkers`() {
    val expectedOutput = readFile("testdata/expectation/prisoners-with-assigned-workers.json")
    val prisonId = "MDI"
    prisonerSearchApiMockServer.stubGetPrisonersList(prisonId, 500, 0, "", 200)
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?term=&page=0&size=10&sort=releaseDate,DESC&includePastReleaseDates=true")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, JsonCompareMode.STRICT)
  }

  @Test
  fun `Get All Prisoners - 400 lastReportCompleted not a valid value`() {
    val prisonId = "MDI"
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?term=&page=0&size=10&sort=releaseDate,DESC&lastReportCompleted=TEST")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(400)
  }

  @Test
  fun `Get All Prisoners happy path - lastReportCompleted is a valid value`() {
    val prisonId = "MDI"
    val validLastReportCompleted = listOf("BCST2", "RESETTLEMENT_PLAN", "NONE").random()
    webTestClient.get()
      .uri("/resettlement-passport/prison/$prisonId/prisoners?term=&page=0&size=10&sort=releaseDate,DESC&lastReportCompleted=$validLastReportCompleted")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
  }
}
