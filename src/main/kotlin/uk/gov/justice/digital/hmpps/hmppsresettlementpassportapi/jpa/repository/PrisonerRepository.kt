package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity

@Repository
interface PrisonerRepository : JpaRepository<PrisonerEntity, Long> {

  fun findByNomsId(nomsId: String): PrisonerEntity?

  @Query("select distinct prisonId from PrisonerEntity order by prisonId asc")
  fun findDistinctPrisonIds(): List<String>

  fun findAllBySupportNeedsLegacyProfileIsTrue(): List<PrisonerEntity>
}
