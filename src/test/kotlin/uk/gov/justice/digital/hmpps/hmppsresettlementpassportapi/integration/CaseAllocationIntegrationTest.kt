package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockkStatic
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseAllocation
import java.time.LocalDateTime

class CaseAllocationIntegrationTest : IntegrationTestBase() {

  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @Test
  @Sql("classpath:testdata/sql/seed-1-prisoner.sql")
  fun `Assign, UnAssign Case Allocation- happy path`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    manageUsersApiMockServer.stubGetManageUsersData("MDI", 200)

    val expectedOutput1 = readFile("testdata/expectation/case-allocation-post-result.json")
    val expectedOutput2 = readFile("testdata/expectation/case-allocation-patch-result.json")
    val staffId = 485931

    webTestClient.get()
      .uri("/resettlement-passport/workers/cases/$staffId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json("[]")

    webTestClient.post()
      .uri("/resettlement-passport/workers/cases")
      .bodyValue(
        CaseAllocation(
          nomsIds = arrayOf("G4161UF"),
          staffId = staffId,
          prisonId = "MDI",
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput1)

    webTestClient.patch()
      .uri("/resettlement-passport/workers/cases")
      .bodyValue(
        CaseAllocation(
          nomsIds = arrayOf("G4161UF"),
          prisonId = "MDI",
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput2)

    webTestClient.get()
      .uri("/resettlement-passport/workers/cases/$staffId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json("[]")

    val auditQueueMessages = sqsClient.receiveMessage(ReceiveMessageRequest.builder().queueUrl(auditQueueUrl).maxNumberOfMessages(2).build()).get().messages()
    assertThat(ObjectMapper().readValue(auditQueueMessages[0].body(), Map::class.java))
      .usingRecursiveComparison()
      .ignoringFields("when")
      .isEqualTo(mapOf("correlationId" to null, "details" to null, "service" to "hmpps-resettlement-passport-api", "subjectId" to "G4161UF", "subjectType" to "PRISONER_ID", "what" to "CASE_ALLOCATION", "when" to "2025-01-06T13:48:20.391273Z", "who" to "RESETTLEMENTPASSPORT_ADM"))
    assertThat(ObjectMapper().readValue(auditQueueMessages[1].body(), Map::class.java))
      .usingRecursiveComparison()
      .ignoringFields("when")
      .isEqualTo(mapOf("correlationId" to null, "details" to null, "service" to "hmpps-resettlement-passport-api", "subjectId" to "G4161UF", "subjectType" to "PRISONER_ID", "what" to "CASE_UNALLOCATION", "when" to "2025-01-06T13:48:20.391273Z", "who" to "RESETTLEMENTPASSPORT_ADM"))
  }

  @Test
  @Sql("classpath:testdata/sql/seed-1-prisoner.sql")
  fun `Assign Case Allocation for already exists`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    manageUsersApiMockServer.stubGetManageUsersDataEmptyList(200)
    val expectedOutput1 = readFile("testdata/expectation/case-allocation-post-result.json")
    val expectedOutput2 = readFile("testdata/expectation/case-allocation-get-result.json")
    val staffId = 485931

    webTestClient.get()
      .uri("/resettlement-passport/workers/cases/$staffId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json("[]")

    webTestClient.post()
      .uri("/resettlement-passport/workers/cases")
      .bodyValue(
        CaseAllocation(
          nomsIds = arrayOf("G4161UF"),
          staffId = staffId,
          prisonId = "MDI",
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput1)

    webTestClient.post()
      .uri("/resettlement-passport/workers/cases")
      .bodyValue(
        CaseAllocation(
          nomsIds = arrayOf("G4161UF"),
          staffId = staffId,
          prisonId = "MDI",
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput1)

    webTestClient.get()
      .uri("/resettlement-passport/workers/cases/$staffId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(expectedOutput2)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-1-prisoner.sql")
  fun `Unassign Case Allocation for not exists`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow

    webTestClient.patch()
      .uri("/resettlement-passport/workers/cases")
      .bodyValue(
        CaseAllocation(
          nomsIds = arrayOf("G4161UF"),
          prisonId = "MDI",
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-1-prisoner.sql")
  fun `Assign Case Allocation - Forbidden`() {
    webTestClient.post()
      .uri("/resettlement-passport/workers/cases")
      .bodyValue(
        CaseAllocation(
          nomsIds = arrayOf("G4161UF"),
          staffId = 4321,
          prisonId = "MDI",
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-1-prisoner.sql")
  fun `Unassign Case Allocation - Forbidden`() {
    webTestClient.patch()
      .uri("/resettlement-passport/workers/cases")
      .bodyValue(
        CaseAllocation(
          nomsIds = arrayOf("G4161UF"),
          prisonId = "MDI",
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-1-prisoner.sql")
  fun `Get workers list for prison Id`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow

    val expectedOutput = readFile("testdata/expectation/case-allocation-get-workers-result.json")
    val prisonId = "MDI"
    manageUsersApiMockServer.stubGetManageUsersData(prisonId, 200)

    webTestClient.get()
      .uri("/resettlement-passport/workers?prisonId=$prisonId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-1-prisoner.sql")
  fun `Get workers list for prison Id not exists`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow

    val prisonId = "MDI1"
    manageUsersApiMockServer.stubGetManageUsersDataEmptyList(200)
    webTestClient.get()
      .uri("/resettlement-passport/workers?prisonId=$prisonId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json("[]")
  }

  @Test
  fun `Get workers list  - unauthorized`() {
    val prisonId = "MDI"

    webTestClient.get()
      .uri("/resettlement-passport/workers?prisonId=$prisonId")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Get workers list  - forbidden`() {
    val prisonId = "MDI"

    webTestClient.get()
      .uri("/resettlement-passport/workers?prisonId=$prisonId")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-case-allocation.sql")
  fun `Get workers capacity for prison Id with sort`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val expectedOutput = readFile("testdata/expectation/case-allocation-get-workers-capacity-result.json")
    val prisonId = "MDI"
    prisonerSearchApiMockServer.stubGetPrisonersList(prisonId, 500, 0, "", 200)
    webTestClient.get()
      .uri("/resettlement-passport/workers/capacity?prisonId=$prisonId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-case-allocation.sql")
  fun `Get workers capacity for prison Id not exists`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow

    val prisonId = "MDI1"
    webTestClient.get()
      .uri("/resettlement-passport/workers/capacity?prisonId=$prisonId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `Get workers capacity for prison Id  - unauthorized`() {
    val prisonId = "MDI"

    webTestClient.get()
      .uri("/resettlement-passport/workers?prisonId=$prisonId")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Get workers capacity for prison Id  - forbidden`() {
    val prisonId = "MDI"

    webTestClient.get()
      .uri("/resettlement-passport/workers?prisonId=$prisonId")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoners-2.sql")
  fun `Get workers capacity for prison Id with zero assigned`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val prisonId = "MDI"
    val expectedOutput = " {\"unassignedCount\":10,\"assignedList\":[]} "
    prisonerSearchApiMockServer.stubGetPrisonersList(prisonId, 500, 0, "", 200)
    webTestClient.get()
      .uri("/resettlement-passport/workers/capacity?prisonId=$prisonId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-1-prisoner.sql")
  fun `Assign Case Allocation StaffId not exists`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    manageUsersApiMockServer.stubGetManageUsersData("MDI", 200)

    val staffId = 4859311

    webTestClient.post()
      .uri("/resettlement-passport/workers/cases")
      .bodyValue(
        CaseAllocation(
          nomsIds = arrayOf("G4161UF"),
          staffId = staffId,
          prisonId = "MDI",
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("developerMessage").toString().contains("Staff with id 4859311 not found in database")
  }

  @Test
  @Sql("classpath:testdata/sql/seed-1-prisoner.sql")
  fun `Assign Case Allocation PrisonId not exists`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow

    manageUsersApiMockServer.stubGetManageUsersData("MDI", 200)

    val staffId = 4859311

    webTestClient.post()
      .uri("/resettlement-passport/workers/cases")
      .bodyValue(
        CaseAllocation(
          nomsIds = arrayOf("G4161UF"),
          staffId = staffId,
          prisonId = "MDI1",
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("developerMessage").toString().contains("PrisonId MDI1 with case load PSFR_RESETTLEMENT_WORKER not found")
  }

  @Test
  fun `Assign Case Allocation Prisoner not exists`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val expectedOutput1 = readFile("testdata/expectation/case-allocation-post-result-1.json")
    manageUsersApiMockServer.stubGetManageUsersData("MDI", 200)
    prisonerSearchApiMockServer.stubGetPrisonersList("testdata/prisoner-search-api/prisoner-search-volume-test-1.json", "MDI", 500, 0, "", 200)
    val staffId = 485931

    webTestClient.post()
      .uri("/resettlement-passport/workers/cases")
      .bodyValue(
        CaseAllocation(
          nomsIds = arrayOf("G6335WY"),
          staffId = staffId,
          prisonId = "MDI",
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput1)
  }
}
