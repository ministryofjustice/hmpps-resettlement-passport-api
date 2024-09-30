package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.TodoEntity
import java.util.UUID

@Repository
interface TodoRepository : JpaRepository<TodoEntity, Long> {
  fun findAllByPrisonerIdOrderById(prisonerId: Long): List<TodoEntity>

  @Modifying
  @Query(
    """
      delete from TodoEntity t
      where t.id = :id
      and t.prisonerId = (
        select p.id from PrisonerEntity p 
        where p.crn = :crn
    )
    """,
  )
  fun deleteByIdAndCrn(id: UUID, crn: String): Int

  fun findByIdAndPrisonerId(id: UUID, prisonerId: Long): TodoEntity?
}
