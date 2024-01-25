package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.AssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.IdTypeEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDate
import java.time.LocalDateTime

class AssessmentRepositoryTest : RepositoryTestBase() {
  @Autowired
  lateinit var assessmentRepository: AssessmentRepository

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @BeforeEach
  @AfterEach
  fun beforeEach() {
    assessmentRepository.deleteAll()
    prisonerRepository.deleteAll()
  }

  @Test
  fun `test persist new assessment`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.now(), "crn1", "xyz1", LocalDate.parse("2025-01-23"))
    prisonerRepository.save(prisoner)

    val idDocument = setOf(IdTypeEntity(1, "Birth certificate"))

    val assessment = AssessmentEntity(null, prisoner, LocalDateTime.now(), LocalDateTime.now(), isBankAccountRequired = true, isIdRequired = true, idDocument, false, null)

    assessmentRepository.save(assessment)

    val assessmentFromDatabase = assessmentRepository.findAll()[0]

    assertThat(assessmentFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(assessment)
  }

  @Test
  fun `test findByPrisonerAndIsDeleted`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.now(), "crn1", "xyz1", LocalDate.parse("2025-01-23"))
    prisonerRepository.save(prisoner)

    val idDocument = setOf(IdTypeEntity(1, "Birth certificate"))

    val assessment1 = AssessmentEntity(null, prisoner, LocalDateTime.now(), LocalDateTime.now(), isBankAccountRequired = true, isIdRequired = true, idDocument, false, null)
    val assessment2 = AssessmentEntity(null, prisoner, LocalDateTime.now(), LocalDateTime.now(), isBankAccountRequired = true, isIdRequired = true, idDocument, true, LocalDateTime.now())

    assessmentRepository.save(assessment1)
    assessmentRepository.save(assessment2)

    val assessmentFromDatabase = assessmentRepository.findByPrisonerAndIsDeleted(prisoner)

    assertThat(assessmentFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(assessment1)
  }
}
