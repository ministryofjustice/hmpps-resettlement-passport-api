package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.WatchlistEntity

@Repository
interface WatchlistRepository : JpaRepository<WatchlistEntity, Long> {
  @Query("DELETE FROM WatchlistEntity WHERE prisoner.nomsId = :nomsId and staffUsername = :staffUsername")
  fun deleteByNomsIdAndStaffUsername(nomsId: String, staffUsername: String)
}
