package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.BankApplicationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.BankApplicationStatusLogEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDate
import java.time.LocalDateTime

class BankApplicationRepositoryTest : RepositoryTestBase() {
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
    val prisoner = prisonerRepository.save(PrisonerEntity(null, "NOM1234", LocalDateTime.now(), "crn1", "xyz1", LocalDate.parse("2025-01-23")))

    val logs = setOf(BankApplicationStatusLogEntity(null, null, statusChangedTo = "Application Started", changedAtDate = LocalDateTime.now()))

    val application = BankApplicationEntity(null, prisoner.id(), logs, LocalDateTime.now(), LocalDateTime.now(), status = "Application Started", bankName = "Lloyds")

    bankApplicationRepository.save(application)

    val assessmentFromDatabase = bankApplicationRepository.findAll()[0]

    Assertions.assertThat(assessmentFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(application)
  }

  @Test
  fun `test findByPrisonerAndIsDeleted`() {
    val prisoner = prisonerRepository.save(PrisonerEntity(null, "NOM1234", LocalDateTime.now(), "crn1", "xyz1", LocalDate.parse("2025-01-23")))

    val logs1 = setOf(BankApplicationStatusLogEntity(null, null, statusChangedTo = "Application Started", changedAtDate = LocalDateTime.now()))

    val application1 = BankApplicationEntity(null, prisoner.id(), logs1, LocalDateTime.now(), LocalDateTime.now(), status = "Application Started", bankName = "Lloyds")

    val application2 = BankApplicationEntity(null, prisoner.id(), emptySet(), LocalDateTime.now(), LocalDateTime.now(), status = "Application Started", isDeleted = true, deletionDate = LocalDateTime.now(), bankName = "Lloyds")

    bankApplicationRepository.save(application1)
    bankApplicationRepository.save(application2)

    val assessmentFromDatabase = bankApplicationRepository.findByPrisonerIdAndIsDeleted(prisoner.id())

    Assertions.assertThat(assessmentFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(application1)
  }
}
