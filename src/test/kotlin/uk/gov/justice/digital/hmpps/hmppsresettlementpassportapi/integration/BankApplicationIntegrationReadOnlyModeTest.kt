package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.jdbc.Sql
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.BankApplication
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerNeedRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerNeedsRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.SupportNeedsIntegrationTest.Companion.fakeNow
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.FeatureFlagValueProvider
import java.time.LocalDateTime

@TestConfiguration
class BankApplicationTestMockConfig {
  @Bean
  @Primary
  fun featureFlagValueProvider(): FeatureFlagValueProvider = mockk { every { isReadOnlyMode() } returns true }
}

@Import(BankApplicationTestMockConfig::class)
class BankApplicationIntegrationReadOnlyModeTest : IntegrationTestBase() {

  @Test
  @Sql("classpath:testdata/sql/seed-bank-application.sql")
  fun `Create, update and delete bank application - forbidden`() {

    val nomsId = "123"

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/bankapplication")
      .bodyValue(
        BankApplication(applicationSubmittedDate = fakeNow, bankName = "Lloyds"),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isForbidden

    webTestClient.patch()
      .uri("/resettlement-passport/prisoner/$nomsId/bankapplication/1")
      .bodyValue(
        BankApplication(resubmissionDate = fakeNow, bankResponseDate = fakeNow, status = "Account opened"),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isForbidden

    webTestClient.delete()
      .uri("/resettlement-passport/prisoner/$nomsId/bankapplication/1")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isForbidden

  }
}
