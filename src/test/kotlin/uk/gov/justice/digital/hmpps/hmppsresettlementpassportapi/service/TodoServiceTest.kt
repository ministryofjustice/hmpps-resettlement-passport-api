package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.TodoEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.TodoRepository
import java.time.LocalDate
import java.time.LocalTime
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

  @Nested
  inner class GetByPrisonerId {
    private val toDate = LocalDate.of(2025, 4, 11)
    private val fromDate = toDate.minusDays(7)

    @Test
    fun `should search between the start of fromDate and the end of toDate`() {
      Mockito.`when`(todoRepository.findAllByPrisonerIdAndCreationDateBetween(any(), any(), any())).thenReturn(null)

      todoService.getByPrisonerId(1, fromDate, toDate)
      Mockito.verify(todoRepository).findAllByPrisonerIdAndCreationDateBetween(1, fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX))
    }

    @Test
    fun `should return data from repository`() {
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
          creationDate = toDate.atStartOfDay(),
        ),
      )

      Mockito.`when`(todoRepository.findAllByPrisonerIdAndCreationDateBetween(any(), any(), any())).thenReturn(data)

      val response = todoService.getByPrisonerId(1, fromDate, toDate)
      Assertions.assertEquals(data, response)
    }
  }
}
