package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.CaseNoteRetryEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.CaseNoteRetryRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class CaseNoteRetryService(
  private val caseNoteRetryRepository: CaseNoteRetryRepository,
  private val caseNotesService: CaseNotesService,
  private val resettlementPassportDeliusApiService: ResettlementPassportDeliusApiService,
  private val metricsService: MetricsService,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
    const val MAX_RETRIES = 10
  }

  @Transactional
  fun processDeliusCaseNote(caseNote: CaseNoteRetryEntity) {
    // Ask Delius for CRN again in case it's been updated
    val crn = resettlementPassportDeliusApiService.getCrn(caseNote.prisoner.nomsId)

    if (crn != null) {
      val success = caseNotesService.postBCSTCaseNoteToDelius(
        crn = crn,
        prisonCode = caseNote.prisonCode,
        notes = caseNote.notes,
        name = caseNote.author,
        deliusCaseNoteType = caseNote.type,
        description = null,
        // TODO: this needs to be added into the database
      )

      if (!success) {
        log.warn("Retry failed for case note for ${caseNote.prisoner.nomsId} as error from Delius API. Will schedule next retry.")
        scheduleNextRetry(caseNote)
      } else {
        log.info("Case note for ${caseNote.prisoner.nomsId} successfully sent to Delius on retry. removing from list in database.")
        caseNoteRetryRepository.delete(caseNote)
      }
    } else {
      log.warn("Retry failed for case note for ${caseNote.prisoner.nomsId} as no CRN found. Will schedule next retry.")
      scheduleNextRetry(caseNote)
    }
  }

  fun findByPrisoner(prisoner: PrisonerEntity, startDate: LocalDate, endDate: LocalDate): List<CaseNoteRetryEntity> {
    val from = startDate.atStartOfDay()
    val to = endDate.atTime(LocalTime.MAX)

    return caseNoteRetryRepository.findByPrisonerAndOriginalSubmissionDateBetween(prisoner, from, to)
  }

  fun scheduleNextRetry(caseNote: CaseNoteRetryEntity) {
    caseNote.retryCount += 1
    if (caseNote.retryCount < MAX_RETRIES) {
      caseNote.nextRuntime = LocalDateTime.now().plusHours(getFibonacciNumber(caseNote.retryCount))
    } else {
      log.warn("Giving up retrying delius case note for ${caseNote.prisoner.nomsId}")
      metricsService.incrementCounter("delius_case_note_retry_give_up")
      caseNote.nextRuntime = null
    }
    caseNoteRetryRepository.save(caseNote)
  }
}
