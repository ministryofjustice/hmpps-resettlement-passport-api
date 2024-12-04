package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseAllocationCountResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.CaseAllocationEntity

@Repository
interface CaseAllocationRepository : JpaRepository<CaseAllocationEntity, Long> {
  fun findByPrisonerIdAndIsDeleted(prisonerId: Long, isDeleted: Boolean = false): CaseAllocationEntity?

  fun findByStaffIdAndIsDeleted(staffId: Int, isDeleted: Boolean = false): List<CaseAllocationEntity?>

  @Query(
    "SELECT A from CaseAllocationEntity A, PrisonerEntity B " +
      " WHERE A.prisonerId = B.id and B.prisonId=  :prisonId " +
      " AND A.isDeleted=false order by A.staffFirstname, A.staffLastname",
  )
  fun findAllByPrisonId(prisonId: String): List<CaseAllocationEntity?>

  @Query(
    nativeQuery = true,
    value =
    """
      select staff_id as staffId, staff_firstname as firstName, staff_lastname as lastName, casesAssigned
      from (select *, rank() over (partition by staff_id order by id desc)
            from (select ca.id,
                         staff_id,
                         staff_firstname,
                         staff_lastname,
                         count(staff_id) over (partition by staff_id ) as casesAssigned
                  from case_allocation ca,
                       prisoner p
                  where ca.is_deleted = false
                    and ca.prisoner_id = p.id
                    and p.prison_id = :prisonId) counted) ranked
      where ranked.rank = 1
  """,
  )
  fun findCaseCountByPrisonId(prisonId: String): List<CaseAllocationCountResponse?>

  @Query(
    "SELECT count(A) from CaseAllocationEntity A, PrisonerEntity B " +
      " WHERE A.prisonerId = B.id and B.prisonId=  :prisonId " +
      " AND A.isDeleted=false",
  )
  fun findTotalCaseCountByPrisonId(prisonId: String): Int

  @Query(
    "SELECT ca from CaseAllocationEntity ca, PrisonerEntity p " +
      " WHERE ca.prisonerId = p.id  and p.nomsId=:nomsId" +
      " AND ca.isDeleted=false order by ca.creationDate desc",
  )
  fun findByNomsIdAndIsDeleted(nomsId: String, isDeleted: Boolean = false): List<CaseAllocationEntity>
}
