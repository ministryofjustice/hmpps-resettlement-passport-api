package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import java.time.LocalDateTime

interface ResettlementAssessmentRepository : JpaRepository<ResettlementAssessmentEntity, Long> {

  @Query(
    """
      select * from (
        select *, rank() over (partition by pathway order by id desc, created_date desc) as rank from resettlement_assessment
        where assessment_type = :#{#assessmentType.toString()}
        and prisoner_id = :prisonerId
        and is_deleted = false
      ) by_pathway where rank = 1;
    """,
    nativeQuery = true,
  )
  fun findLatestForEachPathway(prisonerId: Long, assessmentType: ResettlementAssessmentType): List<ResettlementAssessmentEntity>

  @Query(
    """
      select * from (
        select *, rank() over (partition by pathway order by id desc, created_date desc) as rank from resettlement_assessment
        where assessment_type = :#{#assessmentType.toString()}
        and prisoner_id = :#{#prisonerId}
        and created_date >= :#{#fromDate}
        and created_date <= :#{#toDate}
        and is_deleted = false
      ) by_pathway where rank = 1;
    """,
    nativeQuery = true,
  )
  fun findLatestForEachPathwayAndCreationDateBetween(
    prisonerId: Long,
    assessmentType: ResettlementAssessmentType,
    fromDate: LocalDateTime,
    toDate: LocalDateTime,
  ): List<ResettlementAssessmentEntity>

  @Query(
    """
      select * from (
        select *, rank() over (partition by assessment_type,pathway order by id desc, created_date desc) as rank from resettlement_assessment
        where prisoner_id = :prisonerId
        and is_deleted = false
      ) by_pathway where rank = 1;
    """,
    nativeQuery = true,
  )
  fun findLatestForEachPathwayAndType(prisonerId: Long): List<ResettlementAssessmentEntity>

  fun findFirstByPrisonerIdAndPathwayAndAssessmentTypeAndDeletedIsFalseOrderByCreationDateDesc(prisonerId: Long, pathway: Pathway, assessmentType: ResettlementAssessmentType): ResettlementAssessmentEntity?

  fun findFirstByPrisonerIdAndPathwayAndAssessmentStatusAndDeletedIsFalseOrderByCreationDateDesc(prisonerId: Long, pathway: Pathway, assessmentStatus: ResettlementAssessmentStatus): ResettlementAssessmentEntity?
  fun findFirstByPrisonerIdAndPathwayAndAssessmentStatusAndDeletedIsFalseOrderByCreationDateAsc(prisonerId: Long, pathway: Pathway, assessmentStatus: ResettlementAssessmentStatus): ResettlementAssessmentEntity?

  fun findFirstByPrisonerIdAndPathwayAndAssessmentStatusAndCreationDateBetweenAndDeletedIsFalseOrderByCreationDateDesc(
    prisonerId: Long,
    pathway: Pathway,
    assessmentStatus: ResettlementAssessmentStatus,
    fromDate: LocalDateTime,
    toDate: LocalDateTime,
  ): ResettlementAssessmentEntity?

  fun findFirstByPrisonerIdAndPathwayAndAssessmentStatusAndCreationDateBetweenAndDeletedIsFalseOrderByCreationDateAsc(
    prisonerId: Long,
    pathway: Pathway,
    assessmentStatus: ResettlementAssessmentStatus,
    fromDate: LocalDateTime,
    toDate: LocalDateTime,
  ): ResettlementAssessmentEntity?

  fun findFirstByPrisonerIdAndPathwayAndAssessmentTypeAndAssessmentStatusInAndDeletedIsFalseOrderByCreationDateDesc(
    prisonerId: Long,
    pathway: Pathway,
    assessmentType: ResettlementAssessmentType,
    assessmentStatus: List<ResettlementAssessmentStatus>,
  ): ResettlementAssessmentEntity?

  @Query("select count(distinct(a.pathway)) from ResettlementAssessmentEntity a join PrisonerEntity p on a.prisonerId = p.id where p.nomsId = :nomsId and a.assessmentType = :assessmentType and a.assessmentStatus = :assessmentStatus and a.deleted = false")
  fun countByNomsIdAndAssessmentTypeAndAssessmentStatus(nomsId: String, assessmentType: ResettlementAssessmentType, assessmentStatus: ResettlementAssessmentStatus): Int

  @Query("select ra.prisonerId from ResettlementAssessmentEntity ra inner join PrisonerEntity p on ra.prisonerId = p.id where p.prisonId = :prisonId and ra.assessmentType = :assessmentType and ra.assessmentStatus = :assessmentStatus and ra.deleted = false group by ra.prisonerId having count(distinct (ra.pathway)) = :numOfPathways")
  fun findPrisonersWithAllAssessmentsInStatus(prisonId: String, assessmentType: ResettlementAssessmentType, assessmentStatus: ResettlementAssessmentStatus, numOfPathways: Int): Set<Long>

  fun findAllByPrisonerIdAndPathwayAndAssessmentStatus(prisonerId: Long, pathway: Pathway, assessmentStatus: ResettlementAssessmentStatus = ResettlementAssessmentStatus.SUBMITTED): List<ResettlementAssessmentEntity>

  fun findFirstByPrisonerIdAndPathwayAndAssessmentStatusInAndDeletedIsFalseOrderByCreationDateDesc(
    prisonerId: Long,
    pathway: Pathway,
    assessmentStatus: List<ResettlementAssessmentStatus>,
  ): ResettlementAssessmentEntity?

  fun findFirstByPrisonerIdAndAssessmentStatusAndDeletedIsFalseAndSubmissionDateIsNotNullOrderBySubmissionDateDesc(
    prisonerId: Long,
    assessmentStatus: ResettlementAssessmentStatus,
  ): ResettlementAssessmentEntity?

  fun findAllByPrisonerIdAndDeletedIsFalse(prisonerId: Long): List<ResettlementAssessmentEntity>

  fun findAllByPrisonerIdAndCreationDateBetween(
    prisonerId: Long,
    fromDate: LocalDateTime,
    toDate: LocalDateTime,
  ): List<ResettlementAssessmentEntity>

  @Query(
    """
    select a.noms_id as nomsId, a.assessment_type as assessmentType, a.created_date as createdDate, a.submission_date as submissionDate from (
        select p.noms_id, ra.assessment_type, ra.created_date, ra.submission_date, rank() over (partition by p.noms_id order by ra.submission_date desc, ra.id) as rank from resettlement_assessment ra
        inner join prisoner p on ra.prisoner_id = p.id
        where p.prison_id = :prisonId and ra.is_deleted = false and ra.assessment_status = 'SUBMITTED'
      ) a where rank = 1;
  """,
    nativeQuery = true,
  )
  fun findLastReportByPrison(prisonId: String): List<LastReportProjection>
}
