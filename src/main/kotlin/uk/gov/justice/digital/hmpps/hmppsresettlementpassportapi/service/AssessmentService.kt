package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.DuplicateDataFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Assessment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.AssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.IdTypeRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDateTime

@Service
class AssessmentService(
  private val assessmentRepository: AssessmentRepository,
  private val prisonerRepository: PrisonerRepository,
  private val idTypeRepository: IdTypeRepository,
) {

  @Transactional
  fun getAssessmentById(id: Long): AssessmentEntity? {
    val assessment = assessmentRepository.findById(id) ?: return null
    return if (assessment.get().isDeleted) null else assessment.get()
  }

  @Transactional
  fun getAssessmentByNomsId(nomsId: String): AssessmentEntity? {
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val assessment = assessmentRepository.findByPrisonerIdAndIsDeleted(prisoner.id()) ?: throw ResourceNotFoundException("assessment not found")
    return if (assessment.isDeleted) null else assessment
  }

  @Transactional
  fun deleteAssessment(assessment: AssessmentEntity) {
    assessment.isDeleted = true
    assessment.deletionDate = LocalDateTime.now()
    assessmentRepository.save(assessment)
  }

  @Transactional
  fun createAssessment(assessment: Assessment): AssessmentEntity {
    val prisoner = prisonerRepository.findByNomsId(assessment.nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id ${assessment.nomsId} not found in database")

    val assessmentExists = assessmentRepository.findByPrisonerIdAndIsDeleted(prisoner.id())
    if (assessmentExists != null) {
      throw DuplicateDataFoundException("Assessment for prisoner with id ${assessment.nomsId} already exists in database")
    }

    val idTypeEntities = idTypeRepository.findAll().filter { it.name in assessment.idDocuments }.toSet()

    val assessmentEntity = AssessmentEntity(
      null,
      prisoner.id(),
      LocalDateTime.now(),
      assessment.assessmentDate,
      assessment.isBankAccountRequired,
      assessment.isIdRequired,
      idTypeEntities,
      false,
      null,
    )
    return assessmentRepository.save(assessmentEntity)
  }

  @Transactional
  fun deleteAssessmentByNomsId(nomsId: String, assessmentId: String) {
    val prisoner = prisonerRepository.findByNomsId(nomsId)
    val assessment = getAssessmentById(assessmentId.toLong()) ?: throw NoDataWithCodeFoundException(
      "Assessment",
      assessmentId,
    )
    if (assessment.prisonerId != prisoner?.id) {
      throw NoDataWithCodeFoundException(
        "Assessment",
        assessmentId,
      )
    }
    deleteAssessment(assessment)
  }
}
