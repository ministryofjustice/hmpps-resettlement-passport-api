package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.WatchlistEntity

@Repository
interface WatchlistRepository : JpaRepository<WatchlistEntity, Long> {
  fun findByPrisonerAndStaffUsername(prisoner: PrisonerEntity?, staffUsername: String): WatchlistEntity?
}
