package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import jakarta.validation.ValidationException
import org.springframework.data.domain.Sort
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
import kotlin.reflect.full.declaredMemberProperties

private val logger = KotlinLogging.logger {}
private val validSortFields = TodoEntity::class.declaredMemberProperties.map { it.name }.toSet()

@Service
class TodoService(
  private val todoRepository: TodoRepository,
  private val prisonerRepository: PrisonerRepository,
) {
  fun createEntry(crn: String, createRequest: TodoRequest): TodoEntity {
    val prisonerRecord = getPrisoner(crn)

    return todoRepository.save(createRequest.toEntity(prisonerRecord.id()))
  }

  fun getList(crn: String, sortField: String? = null, sortDirection: Sort.Direction? = null): List<TodoEntity> {
    val sort = if (sortField != null) {
      if (sortField !in validSortFields) {
        throw ValidationException("Invalid sort field $sortField")
      }
      Sort.by(sortDirection ?: Sort.Direction.ASC, sortField)
    } else {
      Sort.unsorted()
    }

    val prisonerRecord = getPrisoner(crn)
    return todoRepository.findAllByPrisonerIdOrderById(prisonerRecord.id(), sort)
  }

  private fun getPrisoner(crn: String): PrisonerEntity = prisonerRepository.findByCrn(crn)
    ?: throw ResourceNotFoundException("No person found with crn $crn")

  @Transactional
  fun deleteItem(crn: String, id: UUID) {
    val deleteCount = todoRepository.deleteByIdAndCrn(id, crn)
    if (deleteCount == 0) {
      throw ResourceNotFoundException("Todo item not found by $crn/$id")
    }
    if (deleteCount > 1) {
      logger.error { "Multiple rows in delete todo item $crn/$id" }
      throw IllegalStateException("Unexpected multiple items deletion of $crn/$id")
    }
  }

  @Transactional
  fun updateItem(crn: String, id: UUID, request: TodoRequest): TodoEntity {
    val prisonerRecord = getPrisoner(crn)
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
  fun patchItem(crn: String, id: UUID, request: TodoPatchRequest): TodoEntity {
    val prisonerRecord = getPrisoner(crn)
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
