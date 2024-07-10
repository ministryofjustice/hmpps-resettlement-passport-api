package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.CaseNoteRetryRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.CaseNoteRetryService.Companion.MAX_RETRIES
import java.time.LocalDateTime

@Service
class AdminService(private val caseNoteRetryService: CaseNoteRetryService, private val caseNoteRetryRepository: CaseNoteRetryRepository) {

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
}
