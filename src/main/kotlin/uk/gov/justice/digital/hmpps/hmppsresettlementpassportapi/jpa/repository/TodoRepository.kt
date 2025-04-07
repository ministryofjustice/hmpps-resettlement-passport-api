package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.TodoEntity
import java.time.LocalDateTime
import java.util.*

@Repository
interface TodoRepository : JpaRepository<TodoEntity, Long> {
  fun findAllByPrisonerId(prisonerId: Long, sort: Sort = Sort.unsorted()): List<TodoEntity>

  fun findAllByPrisonerIdAndCreationDateBetween(prisonerId: Long, from: LocalDateTime, to: LocalDateTime): List<TodoEntity>

  @Modifying
  @Query(
    """
      delete from TodoEntity t
      where t.id = :id
      and t.prisonerId = (
        select p.id from PrisonerEntity p 
        where p.nomsId = :nomsId
    )
    """,
  )
  fun deleteByIdAndNomsId(id: UUID, nomsId: String): Int

  fun findByIdAndPrisonerId(id: UUID, prisonerId: Long): TodoEntity?
}
