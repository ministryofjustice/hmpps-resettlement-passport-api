package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.SupportNeedRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonerSearchApiService

@Service
class AdminService(
  private val telemetryClient: TelemetryClient,
  private val caseAllocationService: CaseAllocationService,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val prisonerService: PrisonerService,
  private val supportNeedRepository: SupportNeedRepository,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun sendMetricsToAppInsights() {
    log.info("Starting send metrics to app insights")
    val prisonList = prisonerService.getPrisonList()
    prisonList.forEach { prisonId ->
      try {
        val numberOfPrisonersAssigned = caseAllocationService.getNumberOfAssignedPrisoners(prisonId)
        val numberOfPrisonersInPrison = prisonerSearchApiService.findPrisonersBySearchTerm(prisonId, "").size
        val percentageOfPrisonersAssigned =
          (numberOfPrisonersAssigned.toDouble() / numberOfPrisonersInPrison.toDouble()) * 100
        telemetryClient.trackMetric(
          "case_allocation_assigned_prisoners_percentage",
          percentageOfPrisonersAssigned,
          null,
          null,
          null,
          null,
          mapOf(
            "prisonId" to prisonId,
            "numberOfPrisonersAssigned" to numberOfPrisonersAssigned.toString(),
            "totalNumberOfPrisoners" to numberOfPrisonersInPrison.toString(),
          ),
        )
      } catch (e: Exception) {
        log.warn("Failed to send case_allocation_assigned_prisoners_percentage metric for prisoner with code [$prisonId]", e)
      }
    }

    log.info("Finished sending metrics to app insights")
  }
}
