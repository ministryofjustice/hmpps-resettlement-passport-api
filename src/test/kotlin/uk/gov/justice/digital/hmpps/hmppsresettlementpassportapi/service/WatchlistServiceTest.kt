package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CreateAppointment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CreateAppointmentAddress
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.Address
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.AppointmentDelius
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.Fullname
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.Info
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.LocationInfo
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.StaffInfo
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Category
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import java.time.LocalDate
import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.WatchlistEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.WatchlistRepository

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
    val prisonerEntity = PrisonerEntity(1, nomisId, LocalDateTime.now(), "crn", "xyz", LocalDate.now())
    Mockito.`when`(watchlistRepository.findByPrisonerAndStaffUsername(prisonerEntity, staffUsername)).thenReturn(WatchlistEntity(
      id = 55,
      prisoner = prisonerEntity,
      staffUsername = staffUsername,
      creationDate = LocalDateTime.now()
    ))
    Assertions.assertTrue(watchlistService.isPrisonerInWatchList(staffUsername, prisonerEntity))
  }

  @Test
  fun `isPrisonerInWatchlist returns false`() {
    val nomisId = "A8731DY"
    val staffUsername = "Test User"
    val prisonerEntity = PrisonerEntity(1, nomisId, LocalDateTime.now(), "crn", "xyz", LocalDate.now())
    Mockito.`when`(watchlistRepository.findByPrisonerAndStaffUsername(prisonerEntity, staffUsername)).thenReturn(null)
    Assertions.assertFalse(watchlistService.isPrisonerInWatchList(staffUsername, prisonerEntity))
  }
}
