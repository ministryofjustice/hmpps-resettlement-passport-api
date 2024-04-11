package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDate

@Repository
interface PathwayStatusRepository : JpaRepository<PathwayStatusEntity, Long> {
  fun findByPathwayAndPrisoner(pathway: Pathway, prisoner: PrisonerEntity): PathwayStatusEntity?

  @Query("select p from PathwayStatusEntity ps, PrisonerEntity p where ps.status != :status and p.prisonId = :prisonId and p.id = ps.prisoner.id and ((p.releaseDate between :earliestReleaseDate and :latestReleaseDate) or p.releaseDate is null) group by p having count(ps) > 0")
  fun findPrisonersByPrisonIdWithAtLeastOnePathwayNotInNotStarted(prisonId: String, earliestReleaseDate: LocalDate, latestReleaseDate: LocalDate, status: Status = Status.NOT_STARTED): List<PrisonerEntity>

  @Query("select p from PathwayStatusEntity ps, PrisonerEntity p where ps.status = :status and p.prisonId = :prisonId and p.id = ps.prisoner.id and ((p.releaseDate between :earliestReleaseDate and :latestReleaseDate) or p.releaseDate is null) group by p having count(ps) = :numberOfPathways")
  fun findPrisonersByPrisonIdWithAllPathwaysNotStarted(prisonId: String, earliestReleaseDate: LocalDate, latestReleaseDate: LocalDate, status: Status = Status.NOT_STARTED, numberOfPathways: Int = Pathway.entries.size): List<PrisonerEntity>

  @Query("select p from PathwayStatusEntity ps, PrisonerEntity p where ps.status in (:statuses) and p.prisonId = :prisonId and p.id = ps.prisoner.id and ((p.releaseDate between :earliestReleaseDate and :latestReleaseDate) or p.releaseDate is null) group by p having count(ps) = :numberOfPathways")
  fun findPrisonersByPrisonWithAllPathwaysDone(prisonId: String, earliestReleaseDate: LocalDate, latestReleaseDate: LocalDate, statuses: List<Status> = Status.getCompletedStatuses(), numberOfPathways: Int = Pathway.entries.size): List<PrisonerEntity>

  @Query("select ps from PathwayStatusEntity ps inner join PrisonerEntity p on ps.prisoner = p where p.prisonId = :prisonId")
  fun findByPrison(prisonId: String): List<PathwayStatusEntity>
}
