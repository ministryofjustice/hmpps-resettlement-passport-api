package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.CaseNoteRetryEntity
import java.time.LocalDateTime

interface CaseNoteRetryRepository : JpaRepository<CaseNoteRetryEntity, Long> {
  fun findByNextRuntimeBeforeAndRetryCountLessThan(nextRuntime: LocalDateTime, retryCount: Int): List<CaseNoteRetryEntity>
}
