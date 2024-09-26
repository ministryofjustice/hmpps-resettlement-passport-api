package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.TodoEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.TodoRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource.TodoCreateRequest
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class TodoService(
  private val todoRepository: TodoRepository,
  private val prisonerRepository: PrisonerRepository,
) {
  fun createEntry(nomsId: String, createRequest: TodoCreateRequest): TodoEntity {
    val prisonerRecord = getPrisoner(nomsId)

    return todoRepository.save(createRequest.toEntity(prisonerRecord.id()))
  }

  fun getList(nomsId: String): List<TodoEntity> {
    val prisonerRecord = getPrisoner(nomsId)
    return todoRepository.findAllByPrisonerIdOrderById(prisonerRecord.id())
  }

  private fun getPrisoner(nomsId: String): PrisonerEntity = prisonerRepository.findByNomsId(nomsId)
    ?: throw ResourceNotFoundException("No person found with id $nomsId")

  @Transactional
  fun deleteItem(nomsId: String, id: UUID) {
    val deleteCount = todoRepository.deleteByIdAndNomsId(id, nomsId)
    if (deleteCount == 0) {
      throw ResourceNotFoundException("Todo item not found by $nomsId/$id")
    }
    if (deleteCount > 1) {
      logger.error { "Multiple rows in delete todo item $nomsId/$id" }
      throw IllegalStateException("Unexpected multiple items deletion of $nomsId/$id")
    }
  }
}

internal fun TodoCreateRequest.toEntity(prisonerId: Long) = TodoEntity(
  prisonerId = prisonerId,
  task = this.task,
  notes = this.notes,
  dueDate = this.dueDate,
  createdByUrn = this.urn,
)
