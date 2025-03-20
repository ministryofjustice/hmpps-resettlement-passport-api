package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.CaseNoteRetryRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.SupportNeedRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.CaseNoteRetryService.Companion.MAX_RETRIES
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonerSearchApiService
import java.time.LocalDateTime

@Service
class AdminService(
  private val caseNoteRetryService: CaseNoteRetryService,
  private val caseNoteRetryRepository: CaseNoteRetryRepository,
  private val telemetryClient: TelemetryClient,
  private val caseAllocationService: CaseAllocationService,
  private val prisonerSearchApiService: PrisonerSearchApiService,
  private val prisonerService: PrisonerService,
  private val supportNeedRepository: SupportNeedRepository,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun retryFailedDeliusCaseNotes() {
    log.info("Starting retry failed delius case notes process")
    val caseNotesToRetry = caseNoteRetryRepository.findByNextRuntimeBeforeAndRetryCountLessThan(LocalDateTime.now(), MAX_RETRIES)
    caseNotesToRetry.forEach { caseNote ->
      caseNoteRetryService.processDeliusCaseNote(caseNote)
    }
    log.info("Finished retry failed delius case notes process")
  }

  fun sendMetricsToAppInsights() {
    log.info("Starting send metrics to app insights")
    val prisonList = prisonerService.getPrisonList()
    prisonList.forEach { prisonId ->
      try {
        val numberOfPrisonersAssigned = caseAllocationService.getNumberOfAssignedPrisoners(prisonId)
        val numberOfPrisonersInPrison = prisonerSearchApiService.findPrisonersByPrisonId(prisonId).size
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

    val allSupportNeeds = supportNeedRepository.findAll().map { mapOf("supportNeedId" to it.id, "supportNeedTitle" to it.title, "pathway" to it.pathway.name, "section" to it.section, "deleted" to it.deleted) }
    telemetryClient.trackMetric("support_needs_all", allSupportNeeds.size.toDouble(), null, null, null, null, mapOf("supportNeedDetails" to allSupportNeeds.toString()))
    log.info("Finished sending metrics to app insights")
  }
}
