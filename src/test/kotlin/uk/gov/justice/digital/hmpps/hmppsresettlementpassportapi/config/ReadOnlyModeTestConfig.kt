package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.FeatureFlagValueProvider

@TestConfiguration
class ReadOnlyModeTestConfig {
  @Bean
  @Primary
  fun featureFlagValueProvider() = FeatureFlagValueProvider(FeatureFlagProperties(readOnlyMode = true))
}
