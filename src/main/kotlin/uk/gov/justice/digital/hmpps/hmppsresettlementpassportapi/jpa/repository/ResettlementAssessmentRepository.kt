package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType

interface ResettlementAssessmentRepository : JpaRepository<ResettlementAssessmentEntity, Long> {

  @Query(
    """
      select * from (
        select *, rank() over (partition by pathway order by id desc, created_date desc) as rank from resettlement_assessment
        where assessment_type = :#{#assessmentType.toString()}
        and prisoner_id = :#{#prisoner.id}
      ) by_pathway where rank = 1;
    """,
    nativeQuery = true,
  )
  fun findLatestForEachPathway(prisoner: PrisonerEntity, assessmentType: ResettlementAssessmentType): List<ResettlementAssessmentEntity>

  @Query(
    """
      select * from (
        select *, rank() over (partition by assessment_type,pathway order by id desc, created_date desc) as rank from resettlement_assessment
        where prisoner_id = :#{#prisoner.id}
      ) by_pathway where rank = 1;
    """,
    nativeQuery = true,
  )
  fun findLatestForEachPathwayAndType(prisoner: PrisonerEntity): List<ResettlementAssessmentEntity>

  fun findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisoner: PrisonerEntity, pathway: Pathway, assessmentType: ResettlementAssessmentType): ResettlementAssessmentEntity?

  fun findFirstByPrisonerAndPathwayAndAssessmentStatusOrderByCreationDateDesc(prisoner: PrisonerEntity, pathway: Pathway, assessmentStatus: ResettlementAssessmentStatus): ResettlementAssessmentEntity?
  fun findFirstByPrisonerAndPathwayAndAssessmentStatusOrderByCreationDateAsc(prisoner: PrisonerEntity, pathway: Pathway, assessmentStatus: ResettlementAssessmentStatus): ResettlementAssessmentEntity?

  fun findFirstByPrisonerAndPathwayAndAssessmentTypeAndAssessmentStatusInOrderByCreationDateDesc(
    prisoner: PrisonerEntity,
    pathway: Pathway,
    assessmentType: ResettlementAssessmentType,
    assessmentStatus: List<ResettlementAssessmentStatus>,
  ): ResettlementAssessmentEntity?

  @Query("select count(distinct(pathway)) from ResettlementAssessmentEntity where prisoner.nomsId = :nomsId and assessmentType = :assessmentType and assessmentStatus = :assessmentStatus")
  fun countByNomsIdAndAssessmentTypeAndAssessmentStatus(nomsId: String, assessmentType: ResettlementAssessmentType, assessmentStatus: ResettlementAssessmentStatus): Int

  @Query("select ra.prisoner from ResettlementAssessmentEntity ra inner join PrisonerEntity p on ra.prisoner = p where p.prisonId = :prisonId and ra.assessmentType = :assessmentType and ra.assessmentStatus = :assessmentStatus group by ra.prisoner having count(distinct (ra.pathway)) = :numOfPathways")
  fun findPrisonersWithAllAssessmentsInStatus(prisonId: String, assessmentType: ResettlementAssessmentType, assessmentStatus: ResettlementAssessmentStatus, numOfPathways: Int): List<PrisonerEntity>
}
