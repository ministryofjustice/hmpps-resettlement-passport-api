package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.TestBase
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
class PathwayRepositoryTest : TestBase() {

  @Autowired
  lateinit var pathwayRepository: PathwayRepository

  @Test
  fun `test get all pathways from database`() {
    val expectedPathways = listOf(
      PathwayEntity(Pathway.ACCOMMODATION.id, "Accommodation", true, LocalDateTime.now()),
      PathwayEntity(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR.id, "Attitudes, thinking and behaviour", true, LocalDateTime.now()),
      PathwayEntity(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY.id, "Children, families and communities", true, LocalDateTime.now()),
      PathwayEntity(Pathway.DRUGS_AND_ALCOHOL.id, "Drugs and alcohol", true, LocalDateTime.now()),
      PathwayEntity(Pathway.EDUCATION_SKILLS_AND_WORK.id, "Education, skills and work", true, LocalDateTime.now()),
      PathwayEntity(Pathway.FINANCE_AND_ID.id, "Finance and ID", true, LocalDateTime.now()),
      PathwayEntity(Pathway.HEALTH.id, "Health", true, LocalDateTime.now()),
    )

    val pathwaysFromDatabase = pathwayRepository.findAll()
    assertThat(pathwaysFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(expectedPathways)
  }
}
