package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseAllocation
import java.time.LocalDateTime

class CaseAllocationIntegrationTest : IntegrationTestBase() {

  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @Test
  @Sql("classpath:testdata/sql/seed-1-prisoner.sql")
  fun `Assign, UnAssign Case Allocation- happy path`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow

    val expectedOutput1 = readFile("testdata/expectation/case-allocation-post-result.json")
    val expectedOutput2 = readFile("testdata/expectation/case-allocation-patch-result.json")
    val staffId = 4321

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
          staffId = 4321,
          staffFirstName = "PSO Firstname",
          staffLastName = "PSO Lastname",
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
  }

  @Test
  @Sql("classpath:testdata/sql/seed-1-prisoner.sql")
  fun `Assign Case Allocation for already exists`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow

    val expectedOutput1 = readFile("testdata/expectation/case-allocation-post-result.json")
    val expectedOutput2 = readFile("testdata/expectation/case-allocation-get-result.json")
    val staffId = 4321

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
          staffId = 4321,
          staffFirstName = "PSO Firstname",
          staffLastName = "PSO Lastname",
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
          staffId = 4321,
          staffFirstName = "PSO Firstname",
          staffLastName = "PSO Lastname",
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
    val nomsId = "G4161UF"

    webTestClient.post()
      .uri("/resettlement-passport/workers/cases")
      .bodyValue(
        CaseAllocation(
          nomsIds = arrayOf("G4161UF"),
          staffId = 4321,
          staffFirstName = "PSO Firstname",
          staffLastName = "PSO Lastname",
        ),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-1-prisoner.sql")
  fun `Unassign Case Allocation - Forbidden`() {
    val nomsId = "G4161UF"

    webTestClient.patch()
      .uri("/resettlement-passport/workers/cases")
      .bodyValue(
        CaseAllocation(
          nomsIds = arrayOf("G4161UF"),
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
      .uri("/resettlement-passport/workers/$prisonId")
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
      .uri("/resettlement-passport/workers/$prisonId")
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
      .uri("/resettlement-passport/workers/$prisonId")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Get workers list  - forbidden`() {
    val prisonId = "MDI"

    webTestClient.get()
      .uri("/resettlement-passport/workers/$prisonId")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }
}
