package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseAllocation
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.CurrentDateTimeMockExtension
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.CurrentDateTimeMockExtension.Companion.mockCurrentTime
import java.time.LocalDateTime

@ExtendWith(CurrentDateTimeMockExtension::class)
class CaseAllocationIntegrationReadOnlyModeTest : ReadOnlyIntegrationTestBase() {

  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  internal fun setUp() {
    mockCurrentTime(fakeNow)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-1-prisoner.sql")
  fun `Assign, UnAssign Case Allocation - forbidden`() {
    manageUsersApiMockServer.stubGetManageUsersData("MDI", 200)

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
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-1-prisoner-case-allocation.sql")
  fun `UnAssign Case Allocation - forbidden`() {
    manageUsersApiMockServer.stubGetManageUsersData("MDI", 200)
    val staffId = 456769

    webTestClient.get()
      .uri("/resettlement-passport/workers/cases/$staffId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json("[{\"id\":1,\"prisonerId\":1,\"staffId\":456769,\"staffFirstname\":\"PSO1 Firstname\",\"staffLastname\":\"PSO1 Lastname\",\"isDeleted\":false,\"creationDate\":\"2023-05-17T16:21:44\",\"deletionDate\":null}]")

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
      .expectStatus().isForbidden
  }
}
