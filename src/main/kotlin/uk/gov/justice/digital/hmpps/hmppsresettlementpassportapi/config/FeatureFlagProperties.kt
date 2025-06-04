package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.feature-flags")
data class FeatureFlagProperties(
  val readOnlyMode: Boolean = false,
)
