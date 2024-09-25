package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.TodoEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.TodoRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.resource.TodoCreateRequest

@Service
class TodoService(
  private val todoRepository: TodoRepository,
  private val prisonerRepository: PrisonerRepository,
) {
  fun createEntry(nomsId: String, createRequest: TodoCreateRequest): TodoEntity {
    val prisonerRecord = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("No person found with id $nomsId")

    return todoRepository.save(createRequest.toEntity(prisonerRecord.id()))
  }
}

internal fun TodoCreateRequest.toEntity(prisonerId: Long) = TodoEntity(
  prisonerId = prisonerId,
  task = this.task,
  notes = this.notes,
  dueDate = this.dueDate,
  createdByUrn = this.urn,
)
