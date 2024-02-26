package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PoPUserResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.OneLoginUserData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PoPUserOTPEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PoPUserOTPRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PoPUserApiService
import java.security.SecureRandom
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class PoPUserOTPServiceTest {
  private lateinit var popUserOTPService: PoPUserOTPService

  @Mock
  private lateinit var popUserOTPRepository: PoPUserOTPRepository

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var popUserApiService: PoPUserApiService

  private val testDate = LocalDateTime.parse("2023-08-25T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    popUserOTPService = PoPUserOTPService(popUserOTPRepository, prisonerRepository, popUserApiService)
  }

  @Test
  fun `test get PoP User OTP - returns PoP User OTP `() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn", "xyz", LocalDate.parse("2025-01-23"))
    val popUserOTPEntity = PoPUserOTPEntity(
      null,
      prisonerEntity,
      fakeNow,
      fakeNow.plusDays(7).withHour(11).withMinute(59).withSecond(59),
      123456,
    )

    Mockito.`when`(popUserOTPRepository.findByPrisoner(any())).thenReturn(popUserOTPEntity)
    val result = popUserOTPService.getOTPByPrisoner(prisonerEntity)
    Assertions.assertEquals(popUserOTPEntity, result)
  }

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

  @Test
  fun `test create Pop User OTP - creates and returns PoP User OTP`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    mockkStatic(SecureRandom::class)
    every {
      SecureRandom.getInstanceStrong().nextLong(999999)
    } returns 123456
    val prisonerEntity = PrisonerEntity(1, "acb", fakeNow, "crn", "xyz", null)
    val popUserOTPEntity = PoPUserOTPEntity(
      null,
      prisonerEntity,
      fakeNow,
      fakeNow.plusDays(7).withHour(23).withMinute(59).withSecond(59),
      654321,
    )

    Mockito.`when`(popUserOTPRepository.findByPrisoner(prisonerEntity)).thenReturn(null)
    Mockito.`when`(popUserOTPRepository.save(any())).thenReturn(popUserOTPEntity)

    val result = popUserOTPService.createPoPUserOTP(prisonerEntity)
    Assertions.assertEquals(popUserOTPEntity, result)
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test create Pop User Verified - Fails OTP Invalid`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val oneLoginUserData = OneLoginUserData("urn1", "123457", "email@test.com")
    Mockito.`when`(popUserOTPRepository.findByOtpAndExpiryDateIsGreaterThan(oneLoginUserData.otp?.toLong() ?: 0, LocalDateTime.now())).thenReturn(null)
    assertThrows<ResourceNotFoundException> { popUserOTPService.getPoPUserVerified(oneLoginUserData) }
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test create Pop User Verified -  valid OTP`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val oneLoginUserData = OneLoginUserData("urn1", "123457", "email@test.com")
    val prisoner = PrisonerEntity(1, "acb", fakeNow, "crn", "xyz", null)
    val popUserResponse = PoPUserResponse(1, "crn1", "NA", "email@test.com", true, fakeNow, fakeNow, "GU1234", "urn1")
    val popUserOTPEntity = PoPUserOTPEntity(
      1,
      prisoner,
      fakeNow,
      fakeNow.plusDays(7).withHour(23).withMinute(59).withSecond(59),
      123457,
    )
    Mockito.`when`(prisoner.id?.let { prisonerRepository.findById(it) }).thenReturn(Optional.of(prisoner))
    Mockito.lenient().`when`(popUserOTPRepository.findByOtpAndExpiryDateIsGreaterThan(oneLoginUserData.otp?.toLong() ?: 0, LocalDateTime.now())).thenReturn(popUserOTPEntity)
    Mockito.`when`(popUserApiService.postPoPUserVerification(oneLoginUserData, Optional.of(prisoner))).thenReturn(popUserResponse)
    val result = popUserOTPService.getPoPUserVerified(oneLoginUserData)
    Assertions.assertEquals(popUserResponse, result)
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test generate otp is 6 digits`() {
    val otp = SecureRandom.getInstanceStrong().nextLong(999999)
    val otpValue = String.format("%06d", otp).reversed().toLong()
    Assertions.assertEquals(otpValue.toString().length, 6)
  }

  @Test
  fun `test create Pop User Verified - Fails OTP Expired`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val oneLoginUserData = OneLoginUserData("urn1", "123457", "email@test.com")
    Mockito.lenient().`when`(popUserOTPRepository.findByOtpAndExpiryDateIsGreaterThan(oneLoginUserData.otp?.toLong() ?: 0, testDate)).thenReturn(null)
    assertThrows<ResourceNotFoundException> { popUserOTPService.getPoPUserVerified(oneLoginUserData) }
    unmockkStatic(LocalDateTime::class)
  }
}
