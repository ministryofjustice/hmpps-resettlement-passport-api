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
  fun `test findByPrisonerIdAndCreationDateBetweenOrderByStatusUpdateDateOrCreationDateDesc`() {
    var currentTime = LocalDateTime.now()
    val timeTicking: () -> Unit = { currentTime = currentTime.plusSeconds(1) }

    val prisoner = prisonerRepository.save(
      PrisonerEntity(
        null,
        "NOM1234",
        currentTime.minusDays(3),
        "xyz1",
      ),
    )
    val idType = IdTypeEntity(1, "Birth certificate")

    val makeApplication: () -> IdApplicationEntity = {
      IdApplicationEntity(
        prisonerId = prisoner.id(),
        idType = idType,
        creationDate = currentTime,
        applicationSubmittedDate = currentTime,
        isPriorityApplication = false,
        haveGro = true,
        isUkNationalBornOverseas = false,
        costOfApplication = BigDecimal(15.00),
      ).also { timeTicking() }
    }
    val makeDeletedApplication: (creationDate: LocalDateTime, deletionDate: LocalDateTime) -> IdApplicationEntity = { creationDate, deletionDate ->
      IdApplicationEntity(
        prisonerId = prisoner.id(),
        idType = idType,
        creationDate = creationDate,
        applicationSubmittedDate = creationDate,
        isPriorityApplication = false,
        haveGro = true,
        isUkNationalBornOverseas = false,
        costOfApplication = BigDecimal(15.00),
        isDeleted = true,
        deletionDate = deletionDate,
      ).also { timeTicking() }
    }

    // created in the past (two days ago), deleted "now"
    val application1 = makeDeletedApplication(currentTime.minusDays(2), currentTime)
    // created today (without deletion)
    val application2 = makeApplication()
    // create today and delete at the same time
    val application3 = makeDeletedApplication(currentTime, currentTime)
    // created in future (+2 days) and then deleted afterward (+3 days)
    val application4 = makeDeletedApplication(currentTime.plusDays(2), currentTime.plusDays(3))

    // save in chronicle order:
    listOf(application1, application2, application3, application4)
      .forEach { idApplicationRepository.save(it) }

    // looking for yesterday till tomorrow
    val (fromDate, toDate) = currentTime.run { minusDays(1) to plusDays(1) }
    // expected results in reverse chronicle order (order by statusUpdateDate or else CreationDate desc)
    // not expected: application 1 is before yesterday, application 4 is after tomorrow
    val expectedApplications = listOf(application3, application2)

    val assessmentFromDatabase = idApplicationRepository.findByPrisonerIdAndCreationDateBetweenOrderByStatusUpdateDateOrCreationDateDesc(prisoner.id(), fromDate, toDate)

    Assertions.assertThat(assessmentFromDatabase)
      .usingRecursiveComparison()
      .ignoringFieldsOfTypes(LocalDateTime::class.java)
      .isEqualTo(expectedApplications)
  }
}
