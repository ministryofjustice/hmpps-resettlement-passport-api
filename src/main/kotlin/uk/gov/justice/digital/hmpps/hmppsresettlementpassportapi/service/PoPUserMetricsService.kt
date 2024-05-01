package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LicenceTag
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PopUserCountMetric
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PopUserCountMetrics
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonRegisterApiService

@Service
class PoPUserMetricsService(
  private val registry: MeterRegistry,
  private val popUserOTPService: PoPUserOTPService,
  private val prisonRegisterApiService: PrisonRegisterApiService,
  private val licenceConditionService: LicenceConditionService,
) {
  private val popUserCountMetrics = PopUserCountMetrics()

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun recordCustomMetrics() {
    recordProbationUsersLicenceConditionMetrics()
  }

  fun recordProbationUsersLicenceConditionMetrics() {
    log.info("Started running scheduled POP User metrics job - LicenceCondition")
    val popUserList = popUserOTPService.getAllOTPs()

    var totalPopUser = 0
    if (popUserList != null) {
      totalPopUser = popUserList.size
    }
    val prisonList = prisonRegisterApiService.getActivePrisonsList()
    if (popUserList != null) {
      var percentStdLicenceCondition: Double
      var percentOtherLicenceCondition: Double
      for (prison in prisonList) {
        var stdLicenceConditionCount = 0
        var otherLicenceConditionCount = 0

        try {
          for (popUser in popUserList) {
            if (popUser.prisoner.prisonId.equals(prison.id)) {
              val licencesConditions = licenceConditionService.getLicenceConditionsByNomsId(popUser.prisoner.nomsId)
              val stdLicenceConditionList = licencesConditions?.standardLicenceConditions
              val otherLicenceConditionList = licencesConditions?.otherLicenseConditions
              if (!stdLicenceConditionList.isNullOrEmpty()) {
                stdLicenceConditionCount += 1
              }
              if (!otherLicenceConditionList.isNullOrEmpty()) {
                otherLicenceConditionCount += 1
              }
            }
          }
          percentStdLicenceCondition = calculatePercentage(stdLicenceConditionCount, totalPopUser)
          percentOtherLicenceCondition = calculatePercentage(otherLicenceConditionCount, totalPopUser)

          val metrics = listOf(
            PopUserCountMetric(LicenceTag.STANDARD, percentStdLicenceCondition),
            PopUserCountMetric(LicenceTag.OTHERS, percentOtherLicenceCondition),
          )
          popUserCountMetrics.metrics[prison] = metrics
          val prisonTag = Tags.of("prison", prison.name)

          popUserCountMetrics.metrics[prison]?.forEachIndexed { i, metric ->
            registry.gauge(
              "missing_licence_conditions_percentage",
              prisonTag
                .and("licenceType", metric.licenceType.label),
              popUserCountMetrics,
            ) {
              it.metrics[prison]?.get(i)?.value?.toDouble()
                ?: throw RuntimeException("Can't find value for metric $metric. This is likely a coding error!")
            }
          }
        } catch (_: ResourceNotFoundException) {
        } catch (ex: Exception) {
          log.warn("Error collecting metrics for popUser Missing Licence Conditions ${prison.name}", ex)
        }
      }
    }
    log.info("Finished running scheduled POP User metrics job - LicenceCondition")
  }

  fun calculatePercentage(count: Int, total: Int): Double {
    return if (count > 0) {
      100.00 - ((count.toDouble() / total) * 100)
    } else {
      100.00
    }
  }
}
