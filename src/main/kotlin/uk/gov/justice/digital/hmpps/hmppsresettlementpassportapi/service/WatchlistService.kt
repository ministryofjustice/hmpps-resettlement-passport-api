package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository

@Service
class WatchlistService(
  private val prisonerRepository: PrisonerRepository,
) {
  @Transactional
  fun createWatchlist(nomsId: String, staffUserId: String) {
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with nomsId $nomsId not found in database")
  }
}
