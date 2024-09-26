package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.TodoEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.TodoRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource.TodoPatchRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource.TodoRequest
import java.time.LocalDateTime
import java.util.UUID

private val logger = KotlinLogging.logger {}

@Service
class TodoService(
  private val todoRepository: TodoRepository,
  private val prisonerRepository: PrisonerRepository,
) {
  fun createEntry(nomsId: String, createRequest: TodoRequest): TodoEntity {
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

  @Transactional
  fun updateItem(nomsId: String, id: UUID, request: TodoRequest): TodoEntity {
    val prisonerRecord = getPrisoner(nomsId)
    val todoItem = todoRepository.findByIdAndPrisonerId(id, prisonerRecord.id())
      ?: throw ResourceNotFoundException("No item found for $id")

    return todoRepository.save(
      todoItem.copy(
        title = request.title,
        notes = request.notes,
        dueDate = request.dueDate,
        updatedByUrn = request.urn,
        updatedAt = LocalDateTime.now(),
      ),
    )
  }

  @Transactional
  fun patchItem(nomsId: String, id: UUID, request: TodoPatchRequest): TodoEntity {
    val prisonerRecord = getPrisoner(nomsId)
    val todoItem = todoRepository.findByIdAndPrisonerId(id, prisonerRecord.id())
      ?: throw ResourceNotFoundException("No item found for $id")

    return todoRepository.save(
      todoItem.copy(
        completed = request.completed,
        updatedByUrn = request.urn,
        updatedAt = LocalDateTime.now(),
      ),
    )
  }
}

internal fun TodoRequest.toEntity(prisonerId: Long) = TodoEntity(
  prisonerId = prisonerId,
  title = this.title,
  notes = this.notes,
  dueDate = this.dueDate,
  createdByUrn = this.urn,
)
