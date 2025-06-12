package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseAllocation
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.FeatureFlagValueProvider
import java.time.LocalDateTime

@TestConfiguration
class CaseAllocationTestMockConfig {
  @Bean
  @Primary
  fun featureFlagValueProvider(): FeatureFlagValueProvider = mockk { every { isReadOnlyMode() } returns true }
}

@Import(CaseAllocationTestMockConfig::class)
class CaseAllocationIntegrationReadOnlyModeTest : IntegrationTestBase() {

  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @Test
  @Sql("classpath:testdata/sql/seed-1-prisoner.sql")
  fun `Assign, UnAssign Case Allocation - forbidden`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
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
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
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
