package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedUpdateEntity

@Repository
interface PrisonerSupportNeedUpdateRepository : JpaRepository<PrisonerSupportNeedUpdateEntity, Long> {
  fun findAllByPrisonerSupportNeedIdAndDeletedIsFalseOrderByCreatedDateDesc(id: Long): List<PrisonerSupportNeedUpdateEntity>
}
