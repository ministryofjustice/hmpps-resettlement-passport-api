package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedEntity
import java.time.LocalDateTime

@Repository
interface PrisonerSupportNeedRepository : JpaRepository<PrisonerSupportNeedEntity, Long> {
  fun findAllByPrisonerIdAndDeletedIsFalse(prisonerId: Long): List<PrisonerSupportNeedEntity>

  @Query("""
    select
			psn.id as prisonerSupportNeedId,
			p.nomsId as nomsId,
			psn.supportNeed.pathway as pathway,
			psn.createdDate as prisonerSupportNeedCreatedDate,
      psn.latestUpdate.id as latestUpdateId,
      psn.latestUpdate.status as latestUpdateStatus,
      psn.latestUpdate.createdDate as latestSupportNeedCreatedDate
        from PrisonerSupportNeedEntity psn
          inner join PrisonerEntity p on psn.prisonerId = p.id
            where p.prisonId = 'MDI' and psn.deleted = false and (psn.latestUpdate.deleted = false or psn.latestUpdate.deleted is null) and psn.supportNeed.excludeFromCount = false
    """)
  fun getPrisonerSupportNeedsByPrisonId(prisonId: String): List<PrisonerSupportNeedWithNomsIdAndLatestUpdateProjection>
}

interface PrisonerSupportNeedWithNomsIdAndLatestUpdateProjection {
  val prisonerSupportNeedId: Long
  val nomsId: String
  val pathway: Pathway
  val prisonerSupportNeedCreatedDate: LocalDateTime
  val latestUpdateId: Long?
  val latestUpdateStatus: SupportNeedStatus?
  val latestUpdateCreatedDate: LocalDateTime?
}
