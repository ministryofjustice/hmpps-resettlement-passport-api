package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

class StaffContactsIntegrationTest : IntegrationTestBase() {
  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `get staff contacts happy path - full data`() {
    val prisonerId = "123"
    val crn = "abc"
    val expectedOutput = readFile("testdata/expectation/staff-contacts-1.json")

    communityApiMockServer.stubGetToCrn("/secure/offenders/crn/$crn/allOffenderManagers?includeProbationAreaTeams=true", 200, "testdata/community-api/offender-managers-1.json")

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/staff-contacts")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `get staff contacts happy path no COM data`() {
    val prisonerId = "123"
    val crn = "abc"
    val expectedOutput = readFile("testdata/expectation/staff-contacts-2.json")

    communityApiMockServer.stubGetToCrn("/secure/offenders/crn/$crn/allOffenderManagers?includeProbationAreaTeams=true", 200, "testdata/community-api/offender-managers-2.json")

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/staff-contacts")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `get staff contacts happy path error from community API`() {
    val prisonerId = "123"
    val crn = "abc"
    val expectedOutput = readFile("testdata/expectation/staff-contacts-2.json")

    // Note that even if an individual call to get a staff contact fail, we should just log this and return no data
    communityApiMockServer.stubGetToCrn("/secure/offenders/crn/$crn/allOffenderManagers?includeProbationAreaTeams=true", 404, null)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/staff-contacts")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  fun `get staff contacts - prisoner missing from database`() {
    val prisonerId = "123"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/staff-contacts")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage").isEqualTo("Resource not found. Check request parameters - Cannot find CRN for NomsId 123 in database")
      .jsonPath("developerMessage").isEqualTo("Cannot find CRN for NomsId 123 in database")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  fun `get staff contacts - unauthorized`() {
    val prisonerId = "123"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/staff-contacts")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `get staff contacts - forbidden`() {
    val prisonerId = "123"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$prisonerId/staff-contacts")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }
}