package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedUpdateEntity
import java.time.LocalDateTime

@Repository
interface PrisonerSupportNeedUpdateRepository : JpaRepository<PrisonerSupportNeedUpdateEntity, Long> {
  fun findAllByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(id: Long): List<PrisonerSupportNeedUpdateEntity>
  fun findAllByPrisonerSupportNeedIdInAndDeletedIsFalse(ids: List<Long>): List<PrisonerSupportNeedUpdateEntity>
  fun findAllByPrisonerSupportNeedIdInAndCreatedDateBetween(ids: List<Long>, from: LocalDateTime, to: LocalDateTime): List<PrisonerSupportNeedUpdateEntity>
}
