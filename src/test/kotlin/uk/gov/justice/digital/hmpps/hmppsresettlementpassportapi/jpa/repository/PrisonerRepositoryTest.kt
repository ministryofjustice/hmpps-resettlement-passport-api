package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.TestBase
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
class PrisonerRepositoryTest : TestBase() {
  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @BeforeEach
  @AfterEach
  fun beforeEach() = prisonerRepository.deleteAll()

  @Test
  fun `test persist new prisoner`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.now(), "crn1")
    prisonerRepository.save(prisoner)

    val prisonerFromDatabase = prisonerRepository.findAll()[0]

    assertThat(prisonerFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(prisoner)
  }
}
