package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity

@Repository
interface PathwayStatusRepository : JpaRepository<PathwayStatusEntity, Long> {
  fun findByPathwayAndPrisoner(pathway: PathwayEntity, prisoner: PrisonerEntity): PathwayStatusEntity?

  @Query("select p.nomsId from PathwayStatusEntity ps, PrisonerEntity p where ps.status.id != :status and p.prisonId = :prisonId and p.id = ps.prisoner.id group by p.nomsId having count(ps) > 0")
  fun findInUsePrisonersByPrisonIdAndStatus(prisonId: String, status: Long): List<String>

  @Query("select p.nomsId from PathwayStatusEntity ps, PrisonerEntity p where ps.status.id = :status and p.prisonId = :prisonId and p.id = ps.prisoner.id group by p.nomsId having count(ps) = 7")
  fun findNotStartedPrisonersByPrisonIdAndStatus(prisonId: String, status: Long): List<String>
}
