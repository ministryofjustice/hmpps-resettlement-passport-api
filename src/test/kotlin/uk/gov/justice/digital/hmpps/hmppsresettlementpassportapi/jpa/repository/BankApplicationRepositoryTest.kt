package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.TestBase
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.BankApplicationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.BankApplicationStatusLogEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDate
import java.time.LocalDateTime

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class BankApplicationRepositoryTest : TestBase() {
  @Autowired
  lateinit var bankApplicationRepository: BankApplicationRepository

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @BeforeEach
  @AfterEach
  fun beforeEach() {
    bankApplicationRepository.deleteAll()
    prisonerRepository.deleteAll()
  }

  @Test
  fun `test persist new assessment`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.now(), "crn1", "xyz1", LocalDate.parse("2025-01-23"))
    prisonerRepository.save(prisoner)

    val logs = setOf(BankApplicationStatusLogEntity(null, null, statusChangedTo = "Application Started", changedAtDate = LocalDateTime.now()))

    val application = BankApplicationEntity(null, prisoner, logs, LocalDateTime.now(), LocalDateTime.now(), status = "Application Started", bankName = "Lloyds")

    bankApplicationRepository.save(application)

    val assessmentFromDatabase = bankApplicationRepository.findAll()[0]

    Assertions.assertThat(assessmentFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(application)
  }

  @Test
  fun `test findByPrisonerAndIsDeleted`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.now(), "crn1", "xyz1", LocalDate.parse("2025-01-23"))
    prisonerRepository.save(prisoner)

    val logs1 = setOf(BankApplicationStatusLogEntity(null, null, statusChangedTo = "Application Started", changedAtDate = LocalDateTime.now()))

    val application1 = BankApplicationEntity(null, prisoner, logs1, LocalDateTime.now(), LocalDateTime.now(), status = "Application Started", bankName = "Lloyds")

    val application2 = BankApplicationEntity(null, prisoner, emptySet(), LocalDateTime.now(), LocalDateTime.now(), status = "Application Started", isDeleted = true, deletionDate = LocalDateTime.now(), bankName = "Lloyds")

    bankApplicationRepository.save(application1)
    bankApplicationRepository.save(application2)

    val assessmentFromDatabase = bankApplicationRepository.findByPrisonerAndIsDeleted(prisoner)

    Assertions.assertThat(assessmentFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(application1)
  }
}
