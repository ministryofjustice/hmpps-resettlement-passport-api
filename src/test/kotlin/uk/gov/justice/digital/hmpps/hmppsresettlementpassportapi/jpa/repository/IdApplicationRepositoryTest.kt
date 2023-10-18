package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.TestBase
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.IdApplicationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.IdTypeEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.math.BigDecimal
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
class IdApplicationRepositoryTest : TestBase() {
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
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.now(), "crn1")
    prisonerRepository.save(prisoner)

    val idType = IdTypeEntity(1, "Birth certificate")

    val application = IdApplicationEntity(
      null,
      prisoner,
      idType,
      LocalDateTime.now(),
      LocalDateTime.now(),
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
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.now(), "crn1")
    prisonerRepository.save(prisoner)

    val idType = IdTypeEntity(1, "Birth certificate")

    val application1 = IdApplicationEntity(
      null,
      prisoner,
      idType,
      LocalDateTime.now(),
      LocalDateTime.now(),
      isPriorityApplication = false,
      haveGro = true,
      isUkNationalBornOverseas = false,
      costOfApplication = BigDecimal(15.00),
    )

    val application2 = IdApplicationEntity(
      null,
      prisoner,
      idType,
      LocalDateTime.now(),
      LocalDateTime.now(),
      isPriorityApplication = false,
      haveGro = true,
      isUkNationalBornOverseas = false,
      costOfApplication = BigDecimal(15.00),
      isDeleted = true,
      deletionDate = LocalDateTime.now(),
    )

    idApplicationRepository.save(application1)
    idApplicationRepository.save(application2)

    val assessmentFromDatabase = idApplicationRepository.findByPrisonerAndIdTypeAndIsDeleted(prisoner, idType)

    Assertions.assertThat(assessmentFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(application1)
  }
}
