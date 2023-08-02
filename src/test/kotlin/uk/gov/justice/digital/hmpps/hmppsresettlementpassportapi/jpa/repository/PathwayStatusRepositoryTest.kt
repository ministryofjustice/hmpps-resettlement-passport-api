package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.TestBase
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.StatusEntity
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
class PathwayStatusRepositoryTest : TestBase() {
  @Autowired
  lateinit var pathwayStatusRepository: PathwayStatusRepository

  @BeforeEach
  @AfterEach
  fun beforeEach() = pathwayStatusRepository.deleteAll()

  @Test
  fun `test create new pathway status`() {
    val pathwayStatus = PathwayStatusEntity(
      null,
      PrisonerEntity(null, "ABC1234", LocalDateTime.now()),
      PathwayEntity(Pathway.ACCOMMODATION.id, "Accommodation", true, LocalDateTime.now()),
      StatusEntity(Status.IN_PROGRESS.id, "In Progress", true, LocalDateTime.now()),
      LocalDateTime.now(),
    )
    pathwayStatusRepository.save(pathwayStatus)

    val pathwayStatusInDatabase = pathwayStatusRepository.findAll()[0]

    Assertions.assertThat(pathwayStatusInDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(pathwayStatus)
  }
}
