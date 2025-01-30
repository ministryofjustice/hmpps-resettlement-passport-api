package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.SupportNeedEntity

@Repository
interface SupportNeedRepository : JpaRepository<SupportNeedEntity, Long> {
  fun findByPathwayAndDeletedIsFalse(pathway: Pathway): List<SupportNeedEntity>
}
