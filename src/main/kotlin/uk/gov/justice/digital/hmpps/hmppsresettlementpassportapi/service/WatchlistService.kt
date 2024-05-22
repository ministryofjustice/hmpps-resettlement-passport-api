package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.WatchlistEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.WatchlistRepository
import java.time.LocalDateTime

@Service
class WatchlistService(
  private val prisonerRepository: PrisonerRepository,
  private val watchlistRepository: WatchlistRepository,
) {
  @Transactional
  fun createWatchlist(nomsId: String, auth: String): WatchlistEntity {
    // using nomsId to find the prisoner entity
    val prisoner = findPrisonerByNomsId(nomsId)

    val staffUsername = findStaffUsernameFromAuth(auth)

    // associating the prisoner with the staff username
    val watchlist = WatchlistEntity(
      id = null,
      prisoner = prisoner,
      staffUsername = staffUsername,
      creationDate = LocalDateTime.now(),
    )

    // saving the watchlist entity
    return watchlistRepository.save(watchlist)
  }

  @Transactional
  fun deleteWatchlist(nomsId: String, auth: String) {
    findPrisonerByNomsId(nomsId)
    val staffUsername = findStaffUsernameFromAuth(auth)
    watchlistRepository.deleteByNomsIdAndStaffUsername(nomsId, staffUsername)
  }

  private fun findStaffUsernameFromAuth(auth: String): String {
    return getClaimFromJWTToken(auth, "sub")
      ?: throw ServerWebInputException("Cannot get name from auth token")
  }

  fun findPrisonerByNomsId(nomsId: String): PrisonerEntity {
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

    return prisoner
  }

  fun isPrisonerInWatchList(staffUsername: String, prisoner: PrisonerEntity?): Boolean {
    return watchlistRepository.findByPrisonerAndStaffUsername(prisoner, staffUsername) != null
  }
}
