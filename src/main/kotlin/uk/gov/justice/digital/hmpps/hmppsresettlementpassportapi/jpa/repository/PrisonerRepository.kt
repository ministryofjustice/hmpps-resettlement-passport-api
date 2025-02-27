package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity

@Repository
interface PrisonerRepository : JpaRepository<PrisonerEntity, Long> {

  fun findByNomsId(nomsId: String): PrisonerEntity?

  @Query("select distinct prisonId from PrisonerEntity order by prisonId asc")
  fun findDistinctPrisonIds(): List<String>

  @Modifying
  @Query(
    """
    update prisoner p set support_needs_legacy_profile = (
      select coalesce(
        (select  
          case 
            when ra.assessment_status = 'SUBMITTED' and ra.assessment_type = 'RESETTLEMENT_PLAN' 
              then true 
              else false 
          end
        from resettlement_assessment ra
          where ra.prisoner_id = p.id and ra.is_deleted = false order by ra.created_date desc limit 1),
      false)
    ) where support_needs_legacy_profile is null
    """,
    nativeQuery = true,
  )
  fun updateProfileResetLegacyProfileFlags()
  fun findAllBySupportNeedsLegacyProfileIsTrue(): List<PrisonerEntity>
}
