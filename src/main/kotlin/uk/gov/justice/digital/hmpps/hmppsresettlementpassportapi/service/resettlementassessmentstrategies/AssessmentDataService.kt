package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository

class AssessmentDataService(
  private val resettlementAssessmentRepository: ResettlementAssessmentRepository,
  private val prisonerRepository: PrisonerRepository,
  private val pathwayStatusRepository: PathwayStatusRepository,
) {
  fun saveAssessment(assessment: ResettlementAssessmentEntity): ResettlementAssessmentEntity {
    println("$prisonerRepository, $assessment")
    val save = resettlementAssessmentRepository.save(assessment)
    println(save)
    return save
  }

  fun loadPrisoner(nomsId: String) = (
    prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    )

  fun getExistingAssessment(
    nomsId: String,
    pathway: Pathway,
    assessmentType: ResettlementAssessmentType,
  ): ResettlementAssessmentEntity? {
    // Obtain prisoner from database, if exists
    val prisonerEntity = loadPrisoner(nomsId)

    // Obtain COMPLETE and SUBMITTED resettlement status entity from database
    val resettlementAssessmentStatusEntities = listOf(ResettlementAssessmentStatus.COMPLETE, ResettlementAssessmentStatus.SUBMITTED)

    return resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeAndAssessmentStatusInOrderByCreationDateDesc(
      prisonerEntity,
      pathway,
      assessmentType,
      resettlementAssessmentStatusEntities,
    )
  }

  fun loadPathwayStatusAnswer(pathway: Pathway, nomsId: String): StringAnswer? {
    val prisonerEntity = loadPrisoner(nomsId)
    val pathwayStatus = pathwayStatusRepository.findByPathwayAndPrisoner(pathway, prisonerEntity) ?: return null

    return StringAnswer(pathwayStatus.status.name)
  }
}
