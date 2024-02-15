package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType

interface ResettlementAssessmentRepository : JpaRepository<ResettlementAssessmentEntity, Long> {
  fun findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisoner: PrisonerEntity, pathwayEntity: PathwayEntity, assessmentType: ResettlementAssessmentType): ResettlementAssessmentEntity?

  fun findFirstByPrisonerAndPathwayAndAssessmentTypeInAndAssessmentStatusInOrderByCreationDateDesc(
    prisoner: PrisonerEntity,
    pathway: PathwayEntity,
    assessmentType: List<ResettlementAssessmentType>,
    assessmentStatus: List<ResettlementAssessmentStatusEntity>,
  ): ResettlementAssessmentEntity?

  fun findByPrisoner(prisoner: PrisonerEntity): ResettlementAssessmentEntity?

  @Query("select count(distinct(pathway)) from ResettlementAssessmentEntity where prisoner.nomsId = :nomsId and assessmentType = :assessmentType and assessmentStatus.id = :assessmentStatus")
  fun countByNomsIdAndAssessmentTypeAndAssessmentStatus(nomsId: String, assessmentType: ResettlementAssessmentType, assessmentStatus: Long): Int

  @Query("select ra.prisoner from ResettlementAssessmentEntity ra inner join PrisonerEntity p on ra.prisoner = p where p.prisonId = :prisonId and ra.assessmentType = :assessmentType and ra.assessmentStatus.id = :assessmentStatus group by ra.prisoner having count(distinct (ra.pathway)) = :numOfPathways")
  fun findPrisonersWithAllAssessmentsInStatus(prisonId: String, assessmentType: ResettlementAssessmentType, assessmentStatus: Long, numOfPathways: Int): List<PrisonerEntity>
}
