package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
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
}
