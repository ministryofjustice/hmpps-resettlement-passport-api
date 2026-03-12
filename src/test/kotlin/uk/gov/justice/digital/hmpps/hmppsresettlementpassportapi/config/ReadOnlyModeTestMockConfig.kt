package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import io.mockk.every
import io.mockk.mockk
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.FeatureFlagValueProvider

@TestConfiguration
class ReadOnlyModeTestMockConfig {
  @Bean
  @Primary
  fun featureFlagValueProvider(): FeatureFlagValueProvider = mockk { every { isReadOnlyMode() } returns true }
}
