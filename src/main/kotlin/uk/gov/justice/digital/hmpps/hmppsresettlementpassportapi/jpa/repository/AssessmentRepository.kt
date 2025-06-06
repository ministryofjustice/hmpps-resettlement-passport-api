package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.AssessmentEntity
import java.time.LocalDateTime

@Repository
interface AssessmentRepository : JpaRepository<AssessmentEntity, Long> {
  fun findByPrisonerIdAndIsDeleted(prisonerId: Long, isDeleted: Boolean = false): AssessmentEntity?

  fun findByPrisonerIdAndCreationDateBetween(
    prisonerId: Long,
    fromDate: LocalDateTime,
    toDate: LocalDateTime,
  ): List<AssessmentEntity>
}
