package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.SupportNeedEntity
import java.time.LocalDateTime

class SupportNeedRepositoryTest : RepositoryTestBase() {

  @Autowired
  lateinit var supportNeedRepository: SupportNeedRepository

  @Test
  @Sql("classpath:testdata/sql/seed-support-needs.sql") // TODO - remove this once RSP-1718 is done
  fun `test can find support needs`() {
    val expectedSupportNeeds = listOf(
      SupportNeedEntity(id = 1, pathway = Pathway.ACCOMMODATION, section = "Section A", title = "Support Need 1", hidden = false, excludeFromCount = false, allowOtherDetail = false, createdDate = LocalDateTime.parse("2024-09-08T13:00"), isDeleted = false, deletedDate = null),
      SupportNeedEntity(id = 2, pathway = Pathway.ACCOMMODATION, section = "Section A", title = "Support Need 2", hidden = false, excludeFromCount = false, allowOtherDetail = false, createdDate = LocalDateTime.parse("2024-09-08T13:00"), isDeleted = false, deletedDate = null),
      SupportNeedEntity(id = 3, pathway = Pathway.ACCOMMODATION, section = "Section A", title = "Support Need 3", hidden = false, excludeFromCount = false, allowOtherDetail = false, createdDate = LocalDateTime.parse("2024-09-08T13:00"), isDeleted = true, deletedDate = LocalDateTime.parse("2024-09-08T14:00")),
      SupportNeedEntity(id = 4, pathway = Pathway.ACCOMMODATION, section = "Section B", title = "Support Need 4", hidden = true, excludeFromCount = true, allowOtherDetail = false, createdDate = LocalDateTime.parse("2024-09-08T13:00"), isDeleted = false, deletedDate = null),
      SupportNeedEntity(id = 5, pathway = Pathway.ACCOMMODATION, section = "Section B", title = "Other", hidden = false, excludeFromCount = false, allowOtherDetail = true, createdDate = LocalDateTime.parse("2024-09-08T13:00"), isDeleted = false, deletedDate = null),
    )

    val supportNeedsFromDatabase = supportNeedRepository.findAll()
    Assertions.assertEquals(expectedSupportNeeds, supportNeedsFromDatabase)
  }
}
