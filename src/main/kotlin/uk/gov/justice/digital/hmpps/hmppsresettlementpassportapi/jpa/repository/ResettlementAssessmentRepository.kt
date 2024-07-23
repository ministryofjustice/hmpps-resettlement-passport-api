package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType

interface ResettlementAssessmentRepository : JpaRepository<ResettlementAssessmentEntity, Long> {

  @Query(
    """
      select * from (
        select *, rank() over (partition by pathway order by id desc, created_date desc) as rank from resettlement_assessment
        where assessment_type = :#{#assessmentType.toString()}
        and prisoner_id = :prisonerId
      ) by_pathway where rank = 1;
    """,
    nativeQuery = true,
  )
  fun findLatestForEachPathway(prisonerId: Long, assessmentType: ResettlementAssessmentType): List<ResettlementAssessmentEntity>

  @Query(
    """
      select * from (
        select *, rank() over (partition by assessment_type,pathway order by id desc, created_date desc) as rank from resettlement_assessment
        where prisoner_id = :prisonerId
      ) by_pathway where rank = 1;
    """,
    nativeQuery = true,
  )
  fun findLatestForEachPathwayAndType(prisonerId: Long): List<ResettlementAssessmentEntity>

  fun findFirstByPrisonerIdAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerId: Long, pathway: Pathway, assessmentType: ResettlementAssessmentType): ResettlementAssessmentEntity?

  fun findFirstByPrisonerIdAndPathwayAndAssessmentStatusOrderByCreationDateDesc(prisonerId: Long, pathway: Pathway, assessmentStatus: ResettlementAssessmentStatus): ResettlementAssessmentEntity?
  fun findFirstByPrisonerIdAndPathwayAndAssessmentStatusOrderByCreationDateAsc(prisonerId: Long, pathway: Pathway, assessmentStatus: ResettlementAssessmentStatus): ResettlementAssessmentEntity?

  fun findFirstByPrisonerIdAndPathwayAndAssessmentTypeAndAssessmentStatusInOrderByCreationDateDesc(
    prisonerId: Long,
    pathway: Pathway,
    assessmentType: ResettlementAssessmentType,
    assessmentStatus: List<ResettlementAssessmentStatus>,
  ): ResettlementAssessmentEntity?

  @Query("select count(distinct(a.pathway)) from ResettlementAssessmentEntity a join PrisonerEntity p on a.prisonerId = p.id where p.nomsId = :nomsId and a.assessmentType = :assessmentType and a.assessmentStatus = :assessmentStatus")
  fun countByNomsIdAndAssessmentTypeAndAssessmentStatus(nomsId: String, assessmentType: ResettlementAssessmentType, assessmentStatus: ResettlementAssessmentStatus): Int

  @Query("select ra.prisonerId from ResettlementAssessmentEntity ra inner join PrisonerEntity p on ra.prisonerId = p.id where p.prisonId = :prisonId and ra.assessmentType = :assessmentType and ra.assessmentStatus = :assessmentStatus group by ra.prisonerId having count(distinct (ra.pathway)) = :numOfPathways")
  fun findPrisonersWithAllAssessmentsInStatus(prisonId: String, assessmentType: ResettlementAssessmentType, assessmentStatus: ResettlementAssessmentStatus, numOfPathways: Int): Set<Long>
}
