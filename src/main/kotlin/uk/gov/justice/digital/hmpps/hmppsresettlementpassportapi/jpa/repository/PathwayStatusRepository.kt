package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity

@Repository
interface PathwayStatusRepository : JpaRepository<PathwayStatusEntity, Long> {
  fun findByPathwayAndPrisonerId(pathway: Pathway, prisonerId: Long): PathwayStatusEntity?

  fun findByPrisonerId(prisonerId: Long): List<PathwayStatusEntity>

  @Query(
    """
    select 
        p.id as prisonerId,
        p.nomsId as nomsId, 
        ps.pathway as pathway,
        ps.status as pathwayStatus,
        ps.updatedDate as updatedDate
    from PathwayStatusEntity ps
    inner join PrisonerEntity p 
    on ps.prisonerId = p.id 
    where p.prisonId = :prisonId
  """,
  )
  fun findByPrison(prisonId: String): List<PrisonerWithStatusProjection>
}
