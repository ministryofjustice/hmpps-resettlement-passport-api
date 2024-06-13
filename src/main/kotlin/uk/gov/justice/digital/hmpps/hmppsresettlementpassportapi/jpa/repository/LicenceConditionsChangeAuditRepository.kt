package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.LicenceConditionChangeAuditEntity

@Repository
interface LicenceConditionsChangeAuditRepository : JpaRepository<LicenceConditionChangeAuditEntity, Long> {

  fun findFirstByPrisonerIdOrderByCreationDateDesc(prisonerId: Long): LicenceConditionChangeAuditEntity?

  @Query(
    value = """
      select * from licence_conditions_change_audit 
      where prisoner_id = (select id from prisoner where noms_id = :nomsId)
      and version = :version
    """,
    nativeQuery = true,
  )
  fun getByNomsIdAndVersion(nomsId: String, version: Int): LicenceConditionChangeAuditEntity?
}
