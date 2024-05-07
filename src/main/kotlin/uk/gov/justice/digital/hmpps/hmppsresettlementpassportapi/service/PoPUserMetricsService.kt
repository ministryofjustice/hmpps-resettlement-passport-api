package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LicenceConditions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LicenceTag
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PopUserCountMetric
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PopUserCountMetrics
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PoPUserApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonRegisterApiService

@Service
class PoPUserMetricsService(
  private val registry: MeterRegistry,
  private val prisonRegisterApiService: PrisonRegisterApiService,
  private val licenceConditionService: LicenceConditionService,
  private val popUserApiService: PoPUserApiService,
  private val prisonerRepository: PrisonerRepository,
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
    val popUserList = popUserApiService.getAllVerifiedPopUsers()
    var totalPopUser = 0
    totalPopUser = popUserList.size
    if (totalPopUser > 0) {
      val prisonList = prisonRegisterApiService.getActivePrisonsList()
      var percentStdLicenceCondition: Double
      var percentOtherLicenceCondition: Double
      for (prison in prisonList) {
        var stdLicenceConditionCount = 0
        var otherLicenceConditionCount = 0
        val prisonersList = prisonerRepository.findByPrisonId(prison.id)
        try {
          var popUserExists = false
          for (popUser in popUserList) {
            val prisoner = prisonersList.filter { it.nomsId == popUser.nomsId }
            if (prisoner.isNotEmpty() && prisoner.size == 1) {
              popUserExists = true
              var licencesConditions: LicenceConditions
              try {
                licencesConditions = licenceConditionService.getLicenceConditionsByNomsId(prisoner[0].nomsId)!!
                if (!licencesConditions.standardLicenceConditions.isNullOrEmpty()) {
                  stdLicenceConditionCount += 1
                }
                if (!licencesConditions.otherLicenseConditions.isNullOrEmpty()) {
                  otherLicenceConditionCount += 1
                }
              } catch (_: ResourceNotFoundException) {
                continue
              }
            }
          }
          if (popUserExists) {
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
          }
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
