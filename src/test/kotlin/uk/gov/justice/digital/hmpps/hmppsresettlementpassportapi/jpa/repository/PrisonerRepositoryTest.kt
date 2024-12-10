package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDateTime

class PrisonerRepositoryTest : RepositoryTestBase() {
  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @Test
  fun `test persist new prisoner`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.now(), "crn1", "xyz1")
    prisonerRepository.save(prisoner)

    val prisonerFromDatabase = prisonerRepository.findAll()[0]

    assertThat(prisonerFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(prisoner)
  }

  @Test
  fun `test findDistinctPrisonIds`() {
    prisonerRepository.saveAll(listOf(
      PrisonerEntity(null, "NOM0001", LocalDateTime.now(), "crn1", "AA1"),
      PrisonerEntity(null, "NOM0002", LocalDateTime.now(), "crn2", "AA3"),
      PrisonerEntity(null, "NOM0003", LocalDateTime.now(), "crn3", "AA2"),
      PrisonerEntity(null, "NOM0004", LocalDateTime.now(), "crn4", "AA2"),
      PrisonerEntity(null, "NOM0005", LocalDateTime.now(), "crn5", "AA2"),
      PrisonerEntity(null, "NOM0006", LocalDateTime.now(), "crn6", "AA1"),
      PrisonerEntity(null, "NOM0007", LocalDateTime.now(), "crn7", "AA4"),
    ))

    assertThat(prisonerRepository.findDistinctPrisonIds()).isEqualTo(listOf("AA1", "AA2", "AA3", "AA4"))
  }
}
