package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ProfileReset
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResetReason
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.FeatureFlagValueProvider

@TestConfiguration
class ProfileResetTestMockConfig {
  @Bean
  @Primary
  fun featureFlagValueProvider(): FeatureFlagValueProvider = mockk { every { isReadOnlyMode() } returns true }
}

@Import(ProfileResetTestMockConfig::class)
class ProfileResetIntegrationReadOnlyModeTest : IntegrationTestBase() {

  @Test
  @Sql("classpath:testdata/sql/seed-profile-reset.sql")
  fun `POST reset profile - forbidden`() {
    val nomsId = "ABC1234"
    val prisonId = "MDI"
    val expectedCaseNotes = "Prepare someone for release reports and support needs reset\\n\\n" +
      "Reason for reset: Some additional details\\n\\n" +
      "Any previous immediate needs and pre-release reports have been saved in our archive, but are no longer visible in PSfR.\\n\\n" +
      "All previous support needs have been removed, but updates are still visible.\\n\\n" +
      "Contact the service desk if you think there's a problem."

    caseNotesApiMockServer.stubPostCaseNotes(
      nomsId = nomsId,
      type = "RESET",
      subType = "GEN",
      text = expectedCaseNotes,
      prisonId = prisonId,
      status = 200,
    )

    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/reset-profile?supportNeedsEnabled=true")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), authSource = "nomis"))
      .bodyValue(
        ProfileReset(
          resetReason = ResetReason.OTHER,
          additionalDetails = "Some additional details",
        ),
      )
      .exchange()
      .expectStatus().isForbidden
  }
}
