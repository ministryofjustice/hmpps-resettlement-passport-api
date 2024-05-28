package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.LicenceConditionChangeAuditEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity

@Repository
interface LicenceConditionsChangeAuditRepository : JpaRepository<LicenceConditionChangeAuditEntity, Long> {
  fun findByPrisoner(prisoner: PrisonerEntity): LicenceConditionChangeAuditEntity?
}
