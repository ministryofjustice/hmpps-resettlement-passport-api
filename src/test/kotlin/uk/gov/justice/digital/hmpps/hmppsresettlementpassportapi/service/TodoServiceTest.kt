package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.TodoEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.TodoRepository
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class TodoServiceTest {

  private lateinit var todoService: TodoService

  @Mock
  private lateinit var todoRepository: TodoRepository

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @BeforeEach
  fun beforeEach() {
    todoService = TodoService(todoRepository, prisonerRepository)
  }

  @Test
  fun `test GetByPrisonerId should return data from repository`() {
    val currentDate = LocalDateTime.now()
    val data = listOf(
      TodoEntity(
        id = UUID.randomUUID(),
        prisonerId = 1,
        title = "title",
        notes = null,
        dueDate = null,
        completed = false,
        createdByUrn = "urn",
        updatedByUrn = "urn",
        creationDate = currentDate,
        updatedAt = currentDate,
      ),
    )

    val expected = listOf(
      TodoService.TodoSarContent(
        title = "title",
        notes = null,
        dueDate = null,
        completed = false,
        createdByUrn = "urn",
        updatedByUrn = "urn",
        creationDate = currentDate,
        updatedAt = currentDate,
      ),
    )

    Mockito.`when`(todoRepository.findAllByPrisonerIdAndCreationDateBetween(any(), any(), any())).thenReturn(data)

    val response = todoService.getByPrisonerId(1, LocalDateTime.now(), LocalDateTime.now())
    Assertions.assertEquals(expected, response)
  }
}
