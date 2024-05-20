package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.WatchlistEntity

@Repository
interface WatchlistRepository : JpaRepository<WatchlistEntity, Long> {
  @Modifying
  @Query("DELETE FROM WatchlistEntity w WHERE w.prisoner.nomsId = :nomsId and w.staffUsername = :staffUsername")
  fun deleteByNomsIdAndStaffUsername(nomsId: String, staffUsername: String)
}
