package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.FeatureFlagProperties

@Service
class FeatureFlagValueProvider(private val featureProperties: FeatureFlagProperties) {
  fun isReadOnlyMode(): Boolean = featureProperties.readOnlyMode
}
