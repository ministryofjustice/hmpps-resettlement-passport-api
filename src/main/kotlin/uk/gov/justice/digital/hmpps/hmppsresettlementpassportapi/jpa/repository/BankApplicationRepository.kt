package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.BankApplicationEntity

@Repository
interface BankApplicationRepository : JpaRepository<BankApplicationEntity, Long> {
  fun findByPrisonerIdAndIsDeleted(prisonerId: Long, isDeleted: Boolean = false): BankApplicationEntity?

  @Query(
    value = """
      select b
      from BankApplicationEntity b
      join PrisonerEntity p on b.prisonerId = p.id
      where p.nomsId = :nomsId
      and b.id = :id
    """,
  )
  fun findByIdAndNomsId(id: Long, nomsId: String): BankApplicationEntity?
}
