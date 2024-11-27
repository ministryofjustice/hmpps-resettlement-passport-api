package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.CaseAllocationEntity

@Repository
interface CaseAllocationRepository : JpaRepository<CaseAllocationEntity, Long> {
  fun findByPrisonerIdAndIsDeleted(prisonerId: Long, isDeleted: Boolean = false): CaseAllocationEntity?

  fun findByStaffIdAndIsDeleted(staffId: Int, isDeleted: Boolean = false): List<CaseAllocationEntity?>
  fun findAllByIsDeleted(isDeleted: Boolean = false): List<CaseAllocationEntity?>

  @Query(
    "SELECT A from CaseAllocationEntity A, PrisonerEntity B " +
      " WHERE A.prisonerId = B.id and B.prisonId=  :prisonId " +
      " AND A.isDeleted=false order by A.staffFirstname, A.staffLastname",
  )
  fun findAllByPrisonId(prisonId: String): List<CaseAllocationEntity?>
}
