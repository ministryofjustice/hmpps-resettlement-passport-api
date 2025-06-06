package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedUpdateEntity
import java.time.LocalDateTime

class PrisonerSupportNeedUpdateRepositoryTest : RepositoryTestBase() {

  @Autowired
  lateinit var prisonerSupportNeedUpdateRepository: PrisonerSupportNeedUpdateRepository

  @Autowired
  lateinit var prisonerSupportNeedRepository: PrisonerSupportNeedRepository

  @Autowired
  lateinit var supportNeedRepository: SupportNeedRepository

  @Test
  @Sql("classpath:testdata/sql/seed-1-prisoner.sql")
  fun `test can save and find prisoner support need updates`() {
    val prisonerSupportNeed = prisonerSupportNeedRepository.save(
      PrisonerSupportNeedEntity(
        prisonerId = 1,
        supportNeed = supportNeedRepository.findById(1).get(),
        otherDetail = null,
        createdBy = "John Smith",
        createdDate = LocalDateTime.parse("2024-04-04T13:00:01"),
      ),
    )

    val update1 = PrisonerSupportNeedUpdateEntity(
      prisonerSupportNeedId = prisonerSupportNeed.id!!,
      createdBy = "John Smith",
      createdDate = LocalDateTime.parse("2024-04-04T13:00:01"),
      updateText = "This is some update text",
      status = SupportNeedStatus.DECLINED,
      isPrison = true,
      isProbation = true,
    )

    val update2 = PrisonerSupportNeedUpdateEntity(
      prisonerSupportNeedId = prisonerSupportNeed.id!!,
      createdBy = "Jane Smith",
      createdDate = LocalDateTime.parse("2024-04-04T14:00:01"),
      updateText = "This is some more update text",
      status = SupportNeedStatus.NOT_STARTED,
      isPrison = false,
      isProbation = false,
    )

    val update3 = PrisonerSupportNeedUpdateEntity(
      prisonerSupportNeedId = prisonerSupportNeed.id!!,
      createdBy = "Jane Smith",
      createdDate = LocalDateTime.parse("2024-04-04T15:00:01"),
      updateText = "This is a deleted update",
      status = SupportNeedStatus.MET,
      isPrison = false,
      isProbation = true,
      deleted = true,
      deletedDate = LocalDateTime.parse("2024-04-04T16:00:01"),
    )

    prisonerSupportNeedUpdateRepository.saveAll(listOf(update1, update2, update3))

    val updatesFromDatabase = prisonerSupportNeedUpdateRepository.findAll()
    Assertions.assertEquals(listOf(update1, update2, update3), updatesFromDatabase)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-4.sql")
  fun `test findAllByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc`() {
    val expectedUpdates = listOf(
      PrisonerSupportNeedUpdateEntity(id = 3, prisonerSupportNeedId = 1, createdBy = "User B", createdDate = LocalDateTime.parse("2024-02-03T09:36:32.713421"), updateText = "This is an update 2", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = false, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 4, prisonerSupportNeedId = 1, createdBy = "User C", createdDate = LocalDateTime.parse("2024-02-02T09:36:32.713421"), updateText = "This is an update 3", status = SupportNeedStatus.MET, isPrison = true, isProbation = true, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 1, prisonerSupportNeedId = 1, createdBy = "User A", createdDate = LocalDateTime.parse("2024-02-01T09:36:32.713421"), updateText = "This is an update 1", status = SupportNeedStatus.NOT_STARTED, isPrison = false, isProbation = false, deleted = false, deletedDate = null),
    )
    Assertions.assertEquals(expectedUpdates, prisonerSupportNeedUpdateRepository.findAllByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(1))
  }

  @Test
  @Sql("classpath:testdata/sql/seed-prisoner-support-needs-4.sql")
  fun `test findAllByPrisonerSupportNeedIdInAndDeletedIsFalse`() {
    val expectedUpdates = listOf(
      PrisonerSupportNeedUpdateEntity(id = 1, prisonerSupportNeedId = 1, createdBy = "User A", createdDate = LocalDateTime.parse("2024-02-01T09:36:32.713421"), updateText = "This is an update 1", status = SupportNeedStatus.NOT_STARTED, isPrison = false, isProbation = false, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 3, prisonerSupportNeedId = 1, createdBy = "User B", createdDate = LocalDateTime.parse("2024-02-03T09:36:32.713421"), updateText = "This is an update 2", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = false, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 4, prisonerSupportNeedId = 1, createdBy = "User C", createdDate = LocalDateTime.parse("2024-02-02T09:36:32.713421"), updateText = "This is an update 3", status = SupportNeedStatus.MET, isPrison = true, isProbation = true, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 5, prisonerSupportNeedId = 3, createdBy = "User C", createdDate = LocalDateTime.parse("2024-02-02T09:36:32.713421"), updateText = "This is an update 4", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = false, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 6, prisonerSupportNeedId = 3, createdBy = "User C", createdDate = LocalDateTime.parse("2024-02-05T09:36:32.713421"), updateText = "This is an update 5", status = SupportNeedStatus.NOT_STARTED, isPrison = true, isProbation = true, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 7, prisonerSupportNeedId = 4, createdBy = "User A", createdDate = LocalDateTime.parse("2024-02-03T10:36:32.713421"), updateText = "This is an update 6", status = SupportNeedStatus.MET, isPrison = true, isProbation = false, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 8, prisonerSupportNeedId = 4, createdBy = "User C", createdDate = LocalDateTime.parse("2024-02-03T09:36:32.713421"), updateText = "This is an update 7", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = true, deleted = false, deletedDate = null),
    )
    Assertions.assertEquals(expectedUpdates, prisonerSupportNeedUpdateRepository.findAllByPrisonerSupportNeedIdInAndDeletedIsFalse(listOf(1, 3, 4)).sortedBy { it.id })
  }

  @Nested
  inner class FindAllByPrisonerSupportNeedIdInAndCreatedDateBetween {

    private val searchDate = LocalDateTime.parse("2024-02-22T09:36:30.713421")

    private val supportNeedUpdates = listOf(
      PrisonerSupportNeedUpdateEntity(id = 1, prisonerSupportNeedId = 2, createdBy = "A user", createdDate = LocalDateTime.parse("2024-02-22T09:36:32.713421"), updateText = "This is an update 1", status = SupportNeedStatus.MET, isPrison = true, isProbation = true, deleted = true, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 2, prisonerSupportNeedId = 2, createdBy = "A user", createdDate = LocalDateTime.parse("2024-02-22T09:36:30.713421"), updateText = "This is an update 2", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = false, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 3, prisonerSupportNeedId = 2, createdBy = "A user", createdDate = LocalDateTime.parse("2024-02-22T09:36:31.713421"), updateText = "This is an update 3", status = SupportNeedStatus.MET, isPrison = false, isProbation = true, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 4, prisonerSupportNeedId = 2, createdBy = "A user", createdDate = LocalDateTime.parse("2024-02-22T09:36:29.713421"), updateText = "This is an update 4", status = SupportNeedStatus.DECLINED, isPrison = false, isProbation = false, deleted = false, deletedDate = null),
    )

    @Test
    @Sql("classpath:testdata/sql/seed-prisoner-support-needs-1.sql")
    fun `should return matching updates within date range`() {
      val searchWithinRange = prisonerSupportNeedUpdateRepository.findAllByPrisonerSupportNeedIdInAndCreatedDateBetween(
        listOf(2, 99, 100),
        searchDate.minusHours(1),
        searchDate.plusHours(1),
      ).sortedBy { it.id }
      Assertions.assertEquals(supportNeedUpdates, searchWithinRange)
    }

    @Test
    @Sql("classpath:testdata/sql/seed-prisoner-support-needs-1.sql")
    fun `should not return updates outside of date range`() {
      val searchOutOfRange = prisonerSupportNeedUpdateRepository.findAllByPrisonerSupportNeedIdInAndCreatedDateBetween(
        listOf(2, 99, 100),
        searchDate.minusHours(2),
        searchDate.minusHours(1),
      )
      Assertions.assertEquals(emptyList<PrisonerSupportNeedUpdateEntity>(), searchOutOfRange)
    }

    @Test
    @Sql("classpath:testdata/sql/seed-prisoner-support-needs-1.sql")
    fun `should not return updates that don't match on support need id`() {
      val searchNoMatchForSupportNeedId = prisonerSupportNeedUpdateRepository.findAllByPrisonerSupportNeedIdInAndCreatedDateBetween(
        listOf(99, 100),
        searchDate.minusHours(1),
        searchDate.plusHours(1),
      )
      Assertions.assertEquals(emptyList<PrisonerSupportNeedUpdateEntity>(), searchNoMatchForSupportNeedId)
    }
  }
}
