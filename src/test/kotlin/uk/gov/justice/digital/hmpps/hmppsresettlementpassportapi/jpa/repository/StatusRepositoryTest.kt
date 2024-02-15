package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.StatusEntity
import java.time.LocalDateTime

class StatusRepositoryTest : RepositoryTestBase() {

  @Autowired
  lateinit var statusRepository: StatusRepository

  @Test
  fun `test get all statuses from database`() {
    val expectedStatuses = listOf(
      StatusEntity(Status.NOT_STARTED.id, "Not Started", true, LocalDateTime.now()),
      StatusEntity(Status.IN_PROGRESS.id, "In Progress", true, LocalDateTime.now()),
      StatusEntity(Status.SUPPORT_NOT_REQUIRED.id, "Support Not Required", true, LocalDateTime.now()),
      StatusEntity(Status.SUPPORT_DECLINED.id, "Support Declined", true, LocalDateTime.now()),
      StatusEntity(Status.DONE.id, "Done", true, LocalDateTime.now()),
      StatusEntity(Status.SUPPORT_REQUIRED.id, "Support Required", true, LocalDateTime.now()),
    )

    val statusesFromDatabase = statusRepository.findAll()
    assertThat(statusesFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(expectedStatuses)
  }
}
