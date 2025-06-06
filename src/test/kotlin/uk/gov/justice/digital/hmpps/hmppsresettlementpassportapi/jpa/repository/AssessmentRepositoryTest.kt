package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.AssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.IdTypeEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDateTime

class AssessmentRepositoryTest : RepositoryTestBase() {
  @Autowired
  lateinit var assessmentRepository: AssessmentRepository

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @Test
  fun `test persist new assessment`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.now(), "xyz1")
    prisonerRepository.save(prisoner)

    val idDocument = setOf(IdTypeEntity(1, "Birth certificate"))

    val assessment = AssessmentEntity(null, prisoner.id(), LocalDateTime.now(), LocalDateTime.now(), isBankAccountRequired = true, isIdRequired = true, idDocument, false, null)

    assessmentRepository.save(assessment)

    val assessmentFromDatabase = assessmentRepository.findAll()[0]

    assertThat(assessmentFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(assessment)
  }

  @Test
  fun `test findByPrisonerAndIsDeleted`() {
    val prisoner = prisonerRepository.save(PrisonerEntity(null, "NOM1234", LocalDateTime.now(), "xyz1"))

    val idDocument = setOf(IdTypeEntity(1, "Birth certificate"))

    val assessment1 = AssessmentEntity(null, prisoner.id(), LocalDateTime.now(), LocalDateTime.now(), isBankAccountRequired = true, isIdRequired = true, idDocument, false, null)
    val assessment2 = AssessmentEntity(null, prisoner.id(), LocalDateTime.now(), LocalDateTime.now(), isBankAccountRequired = true, isIdRequired = true, idDocument, true, LocalDateTime.now())

    assessmentRepository.save(assessment1)
    assessmentRepository.save(assessment2)

    val assessmentFromDatabase = assessmentRepository.findByPrisonerIdAndIsDeleted(prisoner.id())

    assertThat(assessmentFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(assessment1)
  }

  @Test
  fun `test findByPrisonerIdAndCreationDateBetween`() {
    val prisoner = prisonerRepository.save(PrisonerEntity(null, "NOM1234", LocalDateTime.now(), "xyz1"))

    val idDocument = setOf(IdTypeEntity(1, "Birth certificate"))

    val assessment1 = AssessmentEntity(null, prisoner.id(), LocalDateTime.now(), LocalDateTime.now(), isBankAccountRequired = true, isIdRequired = true, idDocument, false, null)
    val assessment2 = AssessmentEntity(null, prisoner.id(), LocalDateTime.now(), LocalDateTime.now(), isBankAccountRequired = true, isIdRequired = true, idDocument, true, LocalDateTime.now())

    assessmentRepository.save(assessment1)
    assessmentRepository.save(assessment2)

    val assessmentFromDatabase = assessmentRepository.findByPrisonerIdAndCreationDateBetween(prisoner.id(), LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1))

    assertThat(assessmentFromDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(listOf(assessment1, assessment2))
  }
}
