package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql

class StaffContactsIntegrationTest : IntegrationTestBase() {

  @AfterEach
  fun afterEach() {
    deliusApiMockServer.resetMappings()
    keyWorkerApiMockServer.resetMappings()
    allocationManagerApiMockServer.resetMappings()
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `get staff contacts happy path - full data`() {
    val prisonerId = "123"
    val crn = "abc"
    val expectedOutput = readFile("testdata/expectation/staff-contacts-1.json")

    deliusApiMockServer.stubGetToCrn("/probation-cases/$crn/community-manager", 200, "testdata/resettlement-passport-delius-api/offender-managers-1.json")
    keyWorkerApiMockServer.stubGet("/key-worker/offender/$prisonerId", 200, "testdata/key-worker-api/key-worker-1.json")
    allocationManagerApiMockServer.stubGet("/api/allocation/$prisonerId", 200, "testdata/allocation-manager-api/poms-1.json")

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

    deliusApiMockServer.stubGetToCrn("/probation-cases/$crn/community-manager", 200, "testdata/resettlement-passport-delius-api/offender-managers-2.json")

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
  fun `get staff contacts happy path - no key worker data`() {
    val prisonerId = "123"
    val expectedOutput = readFile("testdata/expectation/staff-contacts-2.json")

    keyWorkerApiMockServer.stubGet("/key-worker/offender/$prisonerId", 200, "testdata/key-worker-api/key-worker-2.json")

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
  fun `get staff contacts happy path - no POM data`() {
    val prisonerId = "123"
    val expectedOutput = readFile("testdata/expectation/staff-contacts-2.json")

    allocationManagerApiMockServer.stubGet("/api/allocation/$prisonerId", 200, "testdata/allocation-manager-api/poms-2.json")

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
  fun `get staff contacts happy path error from delius API`() {
    val prisonerId = "123"
    val crn = "abc"
    val expectedOutput = readFile("testdata/expectation/staff-contacts-2.json")

    // Note that even if an individual call to get a staff contact fails, we should just log this and return no data
    deliusApiMockServer.stubGetToCrn("/probation-cases/$crn/community-manager", 404, null)

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
  fun `get staff contacts happy path error from key worker API`() {
    val prisonerId = "123"
    val expectedOutput = readFile("testdata/expectation/staff-contacts-2.json")

    // Note that even if an individual call to get a staff contact fails, we should just log this and return no data
    keyWorkerApiMockServer.stubGet("/key-worker/offender/$prisonerId", 404, null)

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
  fun `get staff contacts happy path error from allocation manager API`() {
    val prisonerId = "123"
    val expectedOutput = readFile("testdata/expectation/staff-contacts-2.json")

    // Note that even if an individual call to get a staff contact fails, we should just log this and return no data
    allocationManagerApiMockServer.stubGet("/api/allocation/$prisonerId", 404, null)

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
