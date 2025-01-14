package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.junit.jupiter.api.Assertions
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
  @Sql(scripts = ["classpath:testdata/sql/seed-support-needs.sql", "classpath:testdata/sql/seed-1-prisoner.sql"]) // TODO - remove seed-support-needs.sql once RSP-1718 is done
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
}
