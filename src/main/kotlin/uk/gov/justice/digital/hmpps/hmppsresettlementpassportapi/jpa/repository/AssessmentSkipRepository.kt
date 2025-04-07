package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.AssessmentSkipEntity
import java.time.LocalDateTime

interface AssessmentSkipRepository : JpaRepository<AssessmentSkipEntity, Long> {
  fun findByPrisonerIdAndCreationDateBetween(prisonerId: Long, from: LocalDateTime, to: LocalDateTime): List<AssessmentSkipEntity>
}
