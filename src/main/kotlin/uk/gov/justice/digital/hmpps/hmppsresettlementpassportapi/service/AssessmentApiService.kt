package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.assesmentapi.AssessmentDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.AssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.IdTypeRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDateTime

@Service
class AssessmentApiService(
  private val assessmentRepository: AssessmentRepository,
  private val prisonerRepository: PrisonerRepository,
  private val idTypeRepository: IdTypeRepository,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun getAssessmentById(id: Long): AssessmentEntity? {
    val assessment = assessmentRepository.findById(id) ?: return null
    return if (assessment.get().isDeleted) null else assessment.get()
  }

  suspend fun getAssessmentByNomsId(nomsId: String): AssessmentEntity? {
    var prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val assessment = assessmentRepository.findByPrisoner(prisoner) ?: throw ResourceNotFoundException("assessment not found")
    return if (assessment.isDeleted) null else assessment
  }

  suspend fun deleteAssessment(assessment: AssessmentEntity) {
    assessment.isDeleted = true
    assessment.deletionDate = LocalDateTime.now()
    assessmentRepository.save(assessment)
  }

  suspend fun createAssessment(assessmentDTO: AssessmentDTO): AssessmentEntity {
    var prisoner = prisonerRepository.findByNomsId(assessmentDTO.nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id ${assessmentDTO.nomsId} not found in database")

    val idTypeEntities = idTypeRepository.findAll().filter { it.name in assessmentDTO.idDocuments }.toSet()

    val assessment = AssessmentEntity(
      null,
      prisoner,
      LocalDateTime.now(),
      assessmentDTO.assessmentDate,
      assessmentDTO.isBankAccountRequired,
      assessmentDTO.isIdRequired,
      idTypeEntities,
      false,
      null,
    )
    return assessmentRepository.save(assessment)
  }
}