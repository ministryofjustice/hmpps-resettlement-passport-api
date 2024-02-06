package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatusEntity
import java.time.LocalDateTime

class ResettlementAssessmentStatusRepositoryTest : RepositoryTestBase() {

  @Autowired
  lateinit var statusRepository: ResettlementAssessmentStatusRepository

  @Test
  fun `test get all statuses from database`() {
    val expectedStatuses = listOf(
      ResettlementAssessmentStatusEntity(1, "Not Started", true, LocalDateTime.now()),
      ResettlementAssessmentStatusEntity(2, "In Progress", true, LocalDateTime.now()),
      ResettlementAssessmentStatusEntity(3, "Complete", true, LocalDateTime.now()),
      ResettlementAssessmentStatusEntity(4, "Submitted", true, LocalDateTime.now()),
    )

    val statusesFromDatabase = statusRepository.findAll()
    assertThat(statusesFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(expectedStatuses)
  }
}
