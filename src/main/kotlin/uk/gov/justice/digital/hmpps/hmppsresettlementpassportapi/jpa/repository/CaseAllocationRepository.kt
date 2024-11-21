package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.CaseAllocationEntity

@Repository
interface CaseAllocationRepository : JpaRepository<CaseAllocationEntity, Long> {
  fun findByPrisonerIdAndIsDeleted(prisonerId: Long, isDeleted: Boolean = false): CaseAllocationEntity?

  fun findByStaffIdAndIsDeleted(staffId: Int, isDeleted: Boolean = false): List<CaseAllocationEntity?>
}
