package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.TodoEntity
import java.time.LocalDateTime
import java.util.*

class TodoRepositoryTest : RepositoryTestBase() {

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @Autowired
  lateinit var todoRepository: TodoRepository

  @Test
  fun `test findAllByPrisonerIdAndCreationDateBetween query`() {
    // Seed database with prisoners and to do items
    val prisoner1 = prisonerRepository.save(PrisonerEntity(null, "NOMS1", LocalDateTime.parse("2023-12-13T12:00:00"), "MDI"))
    val prisoner2 = prisonerRepository.save(PrisonerEntity(null, "NOMS2", LocalDateTime.parse("2023-12-13T12:00:00"), "MDI"))

    val searchDate = LocalDateTime.now()

    val todoItems = listOf(
      TodoEntity(
        id = UUID.randomUUID(),
        prisonerId = prisoner2.id(),
        title = "note 1",
        notes = null,
        dueDate = null,
        completed = false,
        createdByUrn = "urn",
        updatedByUrn = "urn",
        creationDate = searchDate.minusDays(10),
      ),
      TodoEntity(
        id = UUID.randomUUID(),
        prisonerId = prisoner2.id(),
        title = "note 2",
        notes = null,
        dueDate = null,
        completed = false,
        createdByUrn = "urn",
        updatedByUrn = "urn",
        creationDate = searchDate.minusDays(5),
      ),
      TodoEntity(
        id = UUID.randomUUID(),
        prisonerId = prisoner2.id(),
        title = "note 3",
        notes = null,
        dueDate = null,
        completed = false,
        createdByUrn = "urn",
        updatedByUrn = "urn",
        creationDate = searchDate,
      ),
    )

    todoRepository.saveAll(todoItems)

    // Prisoner 1 has no to do items
    // Prisoner 2 has three - one is out of search range
    Assertions.assertThat(todoRepository.findAllByPrisonerIdAndCreationDateBetween(prisoner1.id(), searchDate.minusDays(7), searchDate.plusDays(1))).isEmpty()
    Assertions.assertThat(todoRepository.findAllByPrisonerIdAndCreationDateBetween(prisoner2.id(), searchDate.minusDays(7), searchDate.plusDays(1))).isEqualTo(todoItems.filter { it.creationDate > searchDate.minusDays(7) })
  }
}
