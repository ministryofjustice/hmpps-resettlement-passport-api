package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.aop

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.expection.OperationNotAllowedException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.FeatureFlagValueProvider

const val READ_ONLY_MODE_DISABLED = "read-only-mode-disabled"

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresFeature(val featureFlag: String)

@Aspect
@Component
class FeatureFlagAspect(private val featureFlagValueProvider: FeatureFlagValueProvider) {

  @Before("@annotation(requiresFeature)")
  fun checkFeatureFlag(joinPoint: JoinPoint, requiresFeature: RequiresFeature) {
    val enabled = when (requiresFeature.featureFlag) {
      READ_ONLY_MODE_DISABLED -> featureFlagValueProvider.isReadOnlyMode()
      else -> throw IllegalArgumentException("Unknown feature flag: ${requiresFeature.featureFlag}")
    }
    if (enabled) {
      throw OperationNotAllowedException("Feature is disabled due to read-only mode being enabled.")
    }
  }
}
