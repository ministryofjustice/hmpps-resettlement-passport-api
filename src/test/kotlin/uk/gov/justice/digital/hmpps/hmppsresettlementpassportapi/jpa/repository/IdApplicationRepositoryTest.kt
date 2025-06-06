package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.IdApplicationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.IdTypeEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.math.BigDecimal
import java.time.LocalDateTime

class IdApplicationRepositoryTest : RepositoryTestBase() {
  @Autowired
  lateinit var idApplicationRepository: IdApplicationRepository

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @BeforeEach
  @AfterEach
  fun beforeEach() {
    idApplicationRepository.deleteAll()
    prisonerRepository.deleteAll()
  }

  @Test
  fun `test persist new assessment`() {
    val prisoner = prisonerRepository.save(
      PrisonerEntity(
        nomsId = "NOM1234",
        creationDate = LocalDateTime.now(),
        prisonId = "xyz1",
      ),
    )

    val idType = IdTypeEntity(1, "Birth certificate")

    val application = IdApplicationEntity(
      prisonerId = prisoner.id(),
      idType = idType,
      creationDate = LocalDateTime.now(),
      applicationSubmittedDate = LocalDateTime.now(),
      isPriorityApplication = false,
      haveGro = true,
      isUkNationalBornOverseas = false,
      costOfApplication = BigDecimal(15.00),
    )

    idApplicationRepository.save(application)

    val assessmentFromDatabase = idApplicationRepository.findAll()[0]

    Assertions.assertThat(assessmentFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(application)
  }

  @Test
  fun `test findByPrisonerAndIsDeleted`() {
    val prisoner = prisonerRepository.save(
      PrisonerEntity(
        null,
        "NOM1234",
        LocalDateTime.now(),
        "xyz1",
      ),
    )

    val idType = IdTypeEntity(1, "Birth certificate")

    val application1 = IdApplicationEntity(
      prisonerId = prisoner.id(),
      idType = idType,
      creationDate = LocalDateTime.now(),
      applicationSubmittedDate = LocalDateTime.now(),
      isPriorityApplication = false,
      haveGro = true,
      isUkNationalBornOverseas = false,
      costOfApplication = BigDecimal(15.00),
    )

    val application2 = IdApplicationEntity(
      prisonerId = prisoner.id(),
      idType = idType,
      creationDate = LocalDateTime.now(),
      applicationSubmittedDate = LocalDateTime.now(),
      isPriorityApplication = false,
      haveGro = true,
      isUkNationalBornOverseas = false,
      costOfApplication = BigDecimal(15.00),
      isDeleted = true,
      deletionDate = LocalDateTime.now(),
    )

    idApplicationRepository.save(application1)
    idApplicationRepository.save(application2)

    val assessmentFromDatabase = idApplicationRepository.findByPrisonerIdAndIdTypeAndIsDeleted(prisoner.id(), idType)

    Assertions.assertThat(assessmentFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(application1)
  }

  @Test
  fun `test findByPrisonerIdAndCreationDateBetween`() {
    val prisoner = prisonerRepository.save(
      PrisonerEntity(
        null,
        "NOM1234",
        LocalDateTime.now(),
        "xyz1",
      ),
    )

    val idType = IdTypeEntity(1, "Birth certificate")

    val application1 = IdApplicationEntity(
      prisonerId = prisoner.id(),
      idType = idType,
      creationDate = LocalDateTime.now(),
      applicationSubmittedDate = LocalDateTime.now(),
      isPriorityApplication = false,
      haveGro = true,
      isUkNationalBornOverseas = false,
      costOfApplication = BigDecimal(15.00),
    )

    val application2 = IdApplicationEntity(
      prisonerId = prisoner.id(),
      idType = idType,
      creationDate = LocalDateTime.now(),
      applicationSubmittedDate = LocalDateTime.now(),
      isPriorityApplication = false,
      haveGro = true,
      isUkNationalBornOverseas = false,
      costOfApplication = BigDecimal(15.00),
      isDeleted = true,
      deletionDate = LocalDateTime.now(),
    )

    val application3 = IdApplicationEntity(
      prisonerId = prisoner.id(),
      idType = idType,
      creationDate = LocalDateTime.now().minusDays(2),
      applicationSubmittedDate = LocalDateTime.now(),
      isPriorityApplication = false,
      haveGro = true,
      isUkNationalBornOverseas = false,
      costOfApplication = BigDecimal(15.00),
      isDeleted = true,
      deletionDate = LocalDateTime.now(),
    )

    val application4 = IdApplicationEntity(
      prisonerId = prisoner.id(),
      idType = idType,
      creationDate = LocalDateTime.now().plusDays(2),
      applicationSubmittedDate = LocalDateTime.now(),
      isPriorityApplication = false,
      haveGro = true,
      isUkNationalBornOverseas = false,
      costOfApplication = BigDecimal(15.00),
      isDeleted = true,
      deletionDate = LocalDateTime.now(),
    )

    idApplicationRepository.save(application1)
    idApplicationRepository.save(application2)
    idApplicationRepository.save(application3)
    idApplicationRepository.save(application4)

    val assessmentFromDatabase = idApplicationRepository.findByPrisonerIdAndCreationDateBetween(prisoner.id(), LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1))

    Assertions.assertThat(assessmentFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(listOf(application1, application2))
  }
}
