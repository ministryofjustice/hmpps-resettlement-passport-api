package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PoPUserOTPEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PoPUserOTPRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class PoPUserOTPServiceTest {
  private lateinit var popUserOTPService: PoPUserOTPService

  @Mock
  private lateinit var popUserOTPRepository: PoPUserOTPRepository

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    popUserOTPService = PoPUserOTPService(popUserOTPRepository)
  }

  @Test
  fun `test getOTPByPrisoner - returns PoP User OTP`() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn", "xyz", LocalDate.parse("2025-01-23"))
    val popUserOTPEntity = PoPUserOTPEntity(
      null,
      prisonerEntity,
      fakeNow,
      fakeNow.plusDays(7).withHour(11).withMinute(59).withSecond(59),
      123456,
    )
    Mockito.`when`(popUserOTPRepository.findByPrisoner(prisonerEntity)).thenReturn(popUserOTPEntity)
    val result = popUserOTPService.getOTPByPrisoner(prisonerEntity)
    Assertions.assertEquals(popUserOTPEntity, result)
  }

  /*@Test
  fun `test create Pop User OTP - creates and returns PoP User OTP`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val prisonerEntity = PrisonerEntity(1, "acb", fakeNow, "crn", "xyz", null)
    val popUserOTPEntity = PoPUserOTPEntity(
      null,
      prisonerEntity,
      fakeNow,
      fakeNow.plusDays(7).withHour(11).withMinute(59).withSecond(59),
      123456
    )

    Mockito.`when`(popUserOTPRepository.findByPrisoner(any())).thenReturn(null)
    // Mockito.`when`(popUserOTPRepository.delete(any())).thenReturn(false)
    Mockito.`when`(popUserOTPRepository.save(any())).thenReturn(popUserOTPEntity)
    val result = popUserOTPService.createPoPUserOTP(prisonerEntity)
    Mockito.verify(popUserOTPRepository).save(popUserOTPEntity)
    Assertions.assertEquals(popUserOTPEntity, result)
    unmockkStatic(LocalDateTime::class)
  }

   */

  @Test
  fun `test delete PoPUserOTP - hard delete`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn", "xyz", LocalDate.parse("2025-01-23"))
    val popUserOTPEntity = PoPUserOTPEntity(
      null,
      prisonerEntity,
      fakeNow,
      fakeNow.plusDays(7).withHour(11).withMinute(59).withSecond(59),
      123456,
    )

    popUserOTPService.deletePoPUserOTP(popUserOTPEntity)

    Mockito.verify(popUserOTPRepository).delete(popUserOTPEntity)
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test getAll PoP User OTP - returns PoP User OTP List`() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn", "xyz", LocalDate.parse("2025-01-23"))
    // Mockito.`when`(prisonerRepository.findByNomsId(prisonerEntity.nomsId)).thenReturn(prisonerEntity)
    val popUserOTPEntity = PoPUserOTPEntity(
      null,
      prisonerEntity,
      fakeNow,
      fakeNow.plusDays(7).withHour(11).withMinute(59).withSecond(59),
      123456,
    )

    val popUserOTPList = emptyList<PoPUserOTPEntity>().toMutableList()
    popUserOTPList.add(popUserOTPEntity)

    Mockito.`when`(popUserOTPRepository.findAll()).thenReturn(popUserOTPList)
    val result = popUserOTPService.getAllOTPs()
    Assertions.assertEquals(popUserOTPList, result)
  }
}
