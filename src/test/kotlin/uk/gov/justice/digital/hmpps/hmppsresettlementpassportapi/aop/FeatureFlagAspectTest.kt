package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.aop

import org.aspectj.lang.JoinPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.expection.OperationNotAllowedException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.FeatureFlagValueProvider

class FeatureFlagAspectTest {

  private val joinPoint: JoinPoint = mock()
  private val featureFlagValueProvider: FeatureFlagValueProvider = mock()

  @Test
  fun `should allow method when readOnlyMode is false`() {
    whenever(featureFlagValueProvider.isReadOnlyMode()).thenReturn(false)
    val aspect = FeatureFlagAspect(featureFlagValueProvider)
    val annotation = RequiresFeature(READ_ONLY_MODE_DISABLED)

    assertDoesNotThrow {
      aspect.checkFeatureFlag(joinPoint, annotation)
    }
  }

  @Test
  fun `should block method when readOnlyMode is true`() {
    whenever(featureFlagValueProvider.isReadOnlyMode()).thenReturn(true)
    val aspect = FeatureFlagAspect(featureFlagValueProvider)
    val annotation = RequiresFeature(READ_ONLY_MODE_DISABLED)

    assertThrows<OperationNotAllowedException> {
      aspect.checkFeatureFlag(joinPoint, annotation)
    }.apply {
      assertEquals("Feature is disabled due to read-only mode being enabled.", message)
    }
  }

  @Test
  fun `should throw error on unknown feature flag`() {
    whenever(featureFlagValueProvider.isReadOnlyMode()).thenReturn(false)
    val aspect = FeatureFlagAspect(featureFlagValueProvider)
    val annotation = RequiresFeature("unknown-flag")

    assertThrows<IllegalArgumentException> {
      aspect.checkFeatureFlag(joinPoint, annotation)
    }.apply {
      assertEquals("Unknown feature flag: unknown-flag", message)
    }
  }
}
