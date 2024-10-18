package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.WatchlistEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.WatchlistRepository
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class WatchlistServiceTest {

  private lateinit var watchlistService: WatchlistService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var watchlistRepository: WatchlistRepository

  @BeforeEach
  fun beforeEach() {
    watchlistService = WatchlistService(prisonerRepository, watchlistRepository)
  }

  @Test
  fun `isPrisonerInWatchlist returns true`() {
    val nomisId = "A8731DY"
    val staffUsername = "Test User"
    val prisonerEntity = PrisonerEntity(1, nomisId, LocalDateTime.now(), "crn", "xyz")
    Mockito.`when`(watchlistRepository.findByPrisonerIdAndStaffUsername(prisonerEntity.id(), staffUsername)).thenReturn(
      WatchlistEntity(
        id = 55,
        prisonerId = prisonerEntity.id(),
        staffUsername = staffUsername,
        creationDate = LocalDateTime.now(),
      ),
    )
    Assertions.assertTrue(watchlistService.isPrisonerInWatchList(staffUsername, prisonerEntity))
  }

  @Test
  fun `isPrisonerInWatchlist returns false`() {
    val nomisId = "A8731DY"
    val staffUsername = "Test User"
    val prisonerEntity = PrisonerEntity(1, nomisId, LocalDateTime.now(), "crn", "xyz")
    Mockito.`when`(watchlistRepository.findByPrisonerIdAndStaffUsername(prisonerEntity.id(), staffUsername)).thenReturn(null)
    Assertions.assertFalse(watchlistService.isPrisonerInWatchList(staffUsername, prisonerEntity))
  }
}
