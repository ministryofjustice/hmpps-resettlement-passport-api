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
      psnu.id as latestUpdateId,
      psnu.status as latestUpdateStatus,
      psnu.createdDate as latestSupportNeedCreatedDate
        from PrisonerSupportNeedEntity psn
          inner join PrisonerEntity p on psn.prisonerId = p.id
          left join PrisonerSupportNeedUpdateEntity psnu on psnu.id = psn.latestUpdateId
            where p.prisonId = :prisonId and psn.deleted = false and (psnu.deleted = false or psnu.deleted is null) and psn.supportNeed.excludeFromCount = false
    """)
  fun getPrisonerSupportNeedsByPrisonId(prisonId: String): List<Array<Any>>
}
