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
  fun `test findAllByPrisonerIdAndCreationDateBetweenOrderByUpdatedAtDesc query`() {
    // Seed database with prisoners and to do items
    val prisoner1 = prisonerRepository.save(PrisonerEntity(null, "NOMS1", LocalDateTime.parse("2023-12-13T12:00:00"), "MDI"))
    val prisoner2 = prisonerRepository.save(PrisonerEntity(null, "NOMS2", LocalDateTime.parse("2023-12-13T12:00:00"), "MDI"))

    val searchDate = LocalDateTime.now()

    // save in chronicle order: note 1 10 days ago, note 2 5 days ago, note 3 today
    val todoItems = listOf(
      "note 1" to searchDate.minusDays(10),
      "note 2" to searchDate.minusDays(5),
      "note 3" to searchDate,
    ).map { (title, creationDate) ->
      TodoEntity(
        id = UUID.randomUUID(),
        prisonerId = prisoner2.id(),
        title = title,
        notes = null,
        dueDate = null,
        completed = false,
        createdByUrn = "urn",
        updatedByUrn = "urn",
        creationDate = creationDate,
        updatedAt = creationDate,
      )
    }.toList()

    // search range: a week ago till tomorrow
    val (fromDate, toDate) = searchDate.run { minusDays(7) to plusDays(1) }
    // expected results in reverse chronicle order (order by updatedAt desc)
    val expectedTodoItems = todoItems.reversed()
      .filter { it.creationDate > fromDate }

    todoRepository.saveAll(todoItems)

    // Prisoner 1 has no to do items
    // Prisoner 2 has three - one is out of search range
    Assertions.assertThat(todoRepository.findAllByPrisonerIdAndCreationDateBetweenOrderByUpdatedAtDesc(prisoner1.id(), fromDate, toDate)).isEmpty()
    Assertions.assertThat(todoRepository.findAllByPrisonerIdAndCreationDateBetweenOrderByUpdatedAtDesc(prisoner2.id(), fromDate, toDate)).isEqualTo(expectedTodoItems)
  }
}
