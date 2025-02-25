package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.SupportNeedEntity
import java.time.LocalDateTime

class SupportNeedRepositoryTest : RepositoryTestBase() {

  @Autowired
  lateinit var supportNeedRepository: SupportNeedRepository

  @Test
  fun `test can find support needs`() {
    val expectedSupportNeeds = listOf(
      SupportNeedEntity(id = 1, pathway = Pathway.ACCOMMODATION, section = "Accommodation before custody", title = "End a tenancy", hidden = false, excludeFromCount = false, allowOtherDetail = false, createdDate = LocalDateTime.parse("2025-01-14T15:08:55.437998"), deleted = false, deletedDate = null),
      SupportNeedEntity(id = 2, pathway = Pathway.ACCOMMODATION, section = "Accommodation before custody", title = "Maintain a tenancy while in prison", hidden = false, excludeFromCount = false, allowOtherDetail = false, createdDate = LocalDateTime.parse("2025-01-14T15:08:55.437998"), deleted = false, deletedDate = null),
      SupportNeedEntity(id = 3, pathway = Pathway.ACCOMMODATION, section = "Accommodation before custody", title = "Mortgage support while in prison", hidden = false, excludeFromCount = false, allowOtherDetail = false, createdDate = LocalDateTime.parse("2025-01-14T15:08:55.437998"), deleted = false, deletedDate = null),
      SupportNeedEntity(id = 4, pathway = Pathway.ACCOMMODATION, section = "Accommodation before custody", title = "Home adaptations to stay in current accommodation (changes to make it safer and easier to move around and do everyday tasks)", hidden = false, excludeFromCount = false, allowOtherDetail = false, createdDate = LocalDateTime.parse("2025-01-14T15:08:55.437998"), deleted = false, deletedDate = null),
      SupportNeedEntity(id = 5, pathway = Pathway.ACCOMMODATION, section = "Accommodation before custody", title = "Arrange storage for personal possessions while in prison", hidden = false, excludeFromCount = false, allowOtherDetail = false, createdDate = LocalDateTime.parse("2025-01-14T15:08:55.437998"), deleted = false, deletedDate = null),
    )

    val supportNeedsFromDatabase = supportNeedRepository.findAll().filterIndexed { idx, _ -> idx < 5 }
    assertThat(supportNeedsFromDatabase).usingRecursiveComparison().ignoringFields("createdDate").isEqualTo(expectedSupportNeeds)
  }

  @Test
  fun `test findByIdAndDeletedIsFalse`() {
    supportNeedRepository.save(SupportNeedEntity(id = 100001, pathway = Pathway.ACCOMMODATION, section = "Section A", title = "A title", hidden = false, excludeFromCount = false, allowOtherDetail = false, createdDate = LocalDateTime.parse("2025-01-14T15:08:55.437998"), deleted = true, deletedDate = LocalDateTime.parse("2025-01-31T16:14:00")))

    Assertions.assertNull(supportNeedRepository.findByIdAndDeletedIsFalse(100001))
    Assertions.assertNotNull(supportNeedRepository.findByIdAndDeletedIsFalse(1))
  }

  @Test
  fun `test legacy support needs`() {
    // Check that the legacy support needs are the only hidden ones
    val expectedSupportNeeds = listOf(
      SupportNeedEntity(id = 114, pathway = Pathway.ACCOMMODATION, section = "Legacy support need", title = "Accommodation", hidden = true, excludeFromCount = false, allowOtherDetail = false, createdDate = LocalDateTime.parse("2025-02-24T16:53:08.373958"), deleted = false, deletedDate = null),
      SupportNeedEntity(id = 115, pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, section = "Legacy support need", title = "Attitudes, thinking and behaviour", hidden = true, excludeFromCount = false, allowOtherDetail = false, createdDate = LocalDateTime.parse("2025-02-24T16:53:08.373958"), deleted = false, deletedDate = null),
      SupportNeedEntity(id = 116, pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, section = "Legacy support need", title = "Children, families and communities", hidden = true, excludeFromCount = false, allowOtherDetail = false, createdDate = LocalDateTime.parse("2025-02-24T16:53:08.373958"), deleted = false, deletedDate = null),
      SupportNeedEntity(id = 117, pathway = Pathway.DRUGS_AND_ALCOHOL, section = "Legacy support need", title = "Drugs and alcohol", hidden = true, excludeFromCount = false, allowOtherDetail = false, createdDate = LocalDateTime.parse("2025-02-24T16:53:08.373958"), deleted = false, deletedDate = null),
      SupportNeedEntity(id = 118, pathway = Pathway.EDUCATION_SKILLS_AND_WORK, section = "Legacy support need", title = "Education, skills and work", hidden = true, excludeFromCount = false, allowOtherDetail = false, createdDate = LocalDateTime.parse("2025-02-24T16:53:08.373958"), deleted = false, deletedDate = null),
      SupportNeedEntity(id = 119, pathway = Pathway.FINANCE_AND_ID, section = "Legacy support need", title = "Finance and ID", hidden = true, excludeFromCount = false, allowOtherDetail = false, createdDate = LocalDateTime.parse("2025-02-24T16:53:08.373958"), deleted = false, deletedDate = null),
      SupportNeedEntity(id = 120, pathway = Pathway.HEALTH, section = "Legacy support need", title = "Health", hidden = true, excludeFromCount = false, allowOtherDetail = false, createdDate = LocalDateTime.parse("2025-02-24T16:53:08.373958"), deleted = false, deletedDate = null),
    )
    assertThat(supportNeedRepository.findAll().filter { it.hidden }).usingRecursiveComparison().ignoringFields("createdDate").isEqualTo(expectedSupportNeeds)
  }
}
