package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedEntity
import java.time.LocalDateTime

class PrisonerSupportNeedRepositoryTest : RepositoryTestBase() {

  @Autowired
  lateinit var prisonerSupportNeedRepository: PrisonerSupportNeedRepository

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @Test
  @Sql(scripts = ["classpath:testdata/sql/seed-support-needs.sql", "classpath:testdata/sql/seed-1-prisoner.sql"]) // TODO - remove seed-support-needs.sql once RSP-1718 is done
  fun `test can save and find prisoner support needs`() {
    val prisonerSupportNeed1 = PrisonerSupportNeedEntity(
      prisonerId = 1,
      supportNeedId = 1,
      otherDetail = null,
      createdBy = "John Smith",
      createdDate = LocalDateTime.parse("2024-04-04T13:00:01"),
    )

    val prisonerSupportNeed2 = PrisonerSupportNeedEntity(
      prisonerId = 1,
      supportNeedId = 5,
      otherDetail = "Other",
      createdBy = "John Smith",
      createdDate = LocalDateTime.parse("2024-04-04T14:00:01"),
      isDeleted = true,
      deletedDate = LocalDateTime.parse("2023-04-04T15:00:02"),
    )

    prisonerSupportNeedRepository.saveAll(listOf(prisonerSupportNeed1, prisonerSupportNeed2))

    val prisonerSupportNeedsFromDatabase = prisonerSupportNeedRepository.findAll()
    Assertions.assertEquals(listOf(prisonerSupportNeed1, prisonerSupportNeed2), prisonerSupportNeedsFromDatabase)
  }
}
