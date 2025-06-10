package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerNeedRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerNeedsRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.FeatureFlagValueProvider

@TestConfiguration
class SupportNeedsTestMockConfig {
  @Bean
  @Primary
  fun featureFlagValueProvider(): FeatureFlagValueProvider = mockk { every { isReadOnlyMode() } returns true }
}

@Import(SupportNeedsTestMockConfig::class)
class SupportNeedsIntegrationReadOnlyModeTest : IntegrationTestBase() {

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-5.sql")
  fun `test post support needs - forbidden`() {
    val nomsId = "G4161UF"

    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)

    authedWebTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/needs")
      .bodyValue(
        PrisonerNeedsRequest(
          needs = listOf(
            // New need
            PrisonerNeedRequest(
              needId = 8,
              prisonerSupportNeedId = null,
              otherDesc = null,
              text = "This is an update 1",
              status = SupportNeedStatus.NOT_STARTED,
              isPrisonResponsible = true,
              isProbationResponsible = false,
            ),
            // Update of existing need
            PrisonerNeedRequest(
              needId = 1,
              prisonerSupportNeedId = 101,
              otherDesc = null,
              text = "This is an update 2",
              status = SupportNeedStatus.MET,
              isPrisonResponsible = false,
              isProbationResponsible = true,
            ),
            // New need - to be treated as update due to need already being assigned to prisoner
            PrisonerNeedRequest(
              needId = 3,
              prisonerSupportNeedId = null,
              otherDesc = null,
              text = "This is an update 3",
              status = SupportNeedStatus.MET,
              isPrisonResponsible = false,
              isProbationResponsible = true,
            ),
            // New need - other with a new otherDec
            PrisonerNeedRequest(
              needId = 5,
              prisonerSupportNeedId = null,
              otherDesc = "Other 3",
              text = "This is an update 4",
              status = SupportNeedStatus.MET,
              isPrisonResponsible = false,
              isProbationResponsible = true,
            ),
            // New need - "excludeFromCount" i.e. a need with no update
            PrisonerNeedRequest(
              needId = 11,
              prisonerSupportNeedId = null,
              otherDesc = null,
              text = null,
              status = null,
              isPrisonResponsible = null,
              isProbationResponsible = null,
            ),
          ),
        ),
      )
      .exchange()
      .expectStatus().isForbidden
  }
}
