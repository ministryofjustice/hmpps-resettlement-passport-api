package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.FeatureFlagProperties

class FeatureFlagValueProviderTest {

  @Test
  fun `should return true when readOnlyMode is enabled`() {
    val mockProperties = mock<FeatureFlagProperties>()
    whenever(mockProperties.readOnlyMode).thenReturn(true)

    val provider = FeatureFlagValueProvider(mockProperties)

    Assertions.assertTrue(provider.isReadOnlyMode())
  }

  @Test
  fun `should return false when readOnlyMode is disabled`() {
    val mockProperties = mock<FeatureFlagProperties>()
    whenever(mockProperties.readOnlyMode).thenReturn(false)

    val provider = FeatureFlagValueProvider(mockProperties)

    Assertions.assertFalse(provider.isReadOnlyMode())
  }
}
