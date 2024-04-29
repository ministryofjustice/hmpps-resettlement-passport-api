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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Prisoner
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PrisonerPersonal
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.popuserapi.OneLoginData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PoPUserOTPEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PoPUserOTPRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PoPUserApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonerSearchApiService
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class PoPUserOTPServiceTest {
  private lateinit var popUserOTPService: PoPUserOTPService

  @Mock
  private lateinit var popUserOTPRepository: PoPUserOTPRepository

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var popUserApiService: PoPUserApiService

  @Mock
  private lateinit var prisonerSearchApiService: PrisonerSearchApiService

  private val testDate = LocalDateTime.parse("2023-08-25T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    popUserOTPService = PoPUserOTPService(popUserOTPRepository, prisonerRepository, popUserApiService, prisonerSearchApiService)
  }

  @Test
  fun `test get PoP User OTP - returns PoP User OTP `() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn", "xyz", LocalDate.parse("2025-01-23"))
    val popUserOTPEntity = PoPUserOTPEntity(
      null,
      prisonerEntity,
      fakeNow,
      fakeNow.plusDays(7).withHour(11).withMinute(59).withSecond(59),
      "1X3456",
      LocalDate.parse("1982-10-24"),
    )

    Mockito.`when`(popUserOTPRepository.findByPrisoner(any())).thenReturn(popUserOTPEntity)
    val result = popUserOTPService.getOTPByPrisoner(prisonerEntity)
    Assertions.assertEquals(popUserOTPEntity, result)
  }

  @Test
  fun `test get PoP User OTP - returns PoP User not found `() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "crn", "xyz", LocalDate.parse("2025-01-23"))

    Mockito.`when`(popUserOTPRepository.findByPrisoner(any())).thenReturn(null)
    val thrown = assertThrows<ResourceNotFoundException> { popUserOTPService.getOTPByPrisoner(prisonerEntity) }
    Assertions.assertEquals("OTP for Prisoner with id 1 not found in database", thrown.message)
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
      "1X3456",
      LocalDate.parse("1982-10-24"),
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
      "1X3456",
      LocalDate.parse("1982-10-24"),
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
    mockkStatic(::randomAlphaNumericString)
    every {
      randomAlphaNumericString()
    } returns "1X3456"
    val prisonerEntity = PrisonerEntity(1, "acb", fakeNow, "crn", "xyz", null)
    val popUserOTPEntity = PoPUserOTPEntity(
      null,
      prisonerEntity,
      fakeNow,
      fakeNow.plusDays(7).withHour(23).withMinute(59).withSecond(59),
      "1X3456",
      LocalDate.parse("1982-10-24"),
    )

    val prisonerResponse = PrisonersSearch(
      prisonerNumber = "A123456",
      firstName = "firstName",
      lastName = "lastName",
      prisonId = "MDI",
      prisonName = "prisonName",
      cellLocation = null,
      youthOffender = false,
      dateOfBirth = LocalDate.parse("1982-10-24"),
      releaseDate = testDate.toLocalDate().plusMonths(6),
    )

    Mockito.`when`(popUserOTPRepository.findByPrisoner(prisonerEntity)).thenReturn(null)
    Mockito.`when`(popUserOTPRepository.save(any())).thenReturn(popUserOTPEntity)
    Mockito.`when`(prisonerSearchApiService.findPrisonerPersonalDetails(prisonerEntity.nomsId)).thenReturn(prisonerResponse)
    val result = popUserOTPService.createPoPUserOTP(prisonerEntity)
    Assertions.assertEquals(popUserOTPEntity, result)
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test create Pop User Verified - Fails OTP Invalid`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val oneLoginData = OneLoginData("urn1", "123457", "email@test.com", LocalDate.parse("1982-10-24"))
    Mockito.`when`(popUserOTPRepository.findByOtpAndDobAndExpiryDateIsGreaterThan(oneLoginData.otp, LocalDate.parse("1982-10-24"), LocalDateTime.now())).thenReturn(null)
    val thrown = assertThrows<ResourceNotFoundException> { popUserOTPService.getPoPUserVerified(oneLoginData) }
    Assertions.assertEquals("Person On Probation User otp  123457  not found in database or expired.", thrown.message)
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test create Pop User Verified -  valid OTP`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val oneLoginUserData = OneLoginData("urn1", "123457", "email@test.com", LocalDate.parse("1982-10-24"))
    val prisoner = PrisonerEntity(1, "acb", fakeNow, "crn", "xyz", null)
    val popUserResponse = PoPUserResponse(1, "crn1", "NA", true, fakeNow, fakeNow, "GU1234", "urn1")
    val prisonerResponse = PrisonersSearch(
      prisonerNumber = "A123456",
      firstName = "firstName",
      lastName = "lastName",
      prisonId = "MDI",
      prisonName = "prisonName",
      cellLocation = null,
      youthOffender = false,
      dateOfBirth = LocalDate.parse("1982-10-24"),
      releaseDate = testDate.toLocalDate().plusMonths(6),
    )
    val popUserOTPEntity = PoPUserOTPEntity(
      1,
      prisoner,
      fakeNow,
      fakeNow.plusDays(7).withHour(23).withMinute(59).withSecond(59),
      "123457",
      LocalDate.parse("1982-10-24"),
    )
    val prisonerPersonal = PrisonerPersonal(
      prisonerNumber = "A123456",
      firstName = "firstName",
      lastName = "lastName",
      prisonId = "MDI",
      age = 20,
      dateOfBirth = LocalDate.parse("1982-10-24"),
      releaseDate = testDate.toLocalDate().plusMonths(6),
    )
    val prisonerSearch = Prisoner(prisonerPersonal, null, false, false)

    Mockito.`when`(prisoner.id?.let { prisonerRepository.findById(it) }).thenReturn(Optional.of(prisoner))
    Mockito.lenient().`when`(popUserOTPRepository.findByOtpAndDobAndExpiryDateIsGreaterThan(oneLoginUserData.otp, LocalDate.parse("1982-10-24"), LocalDateTime.now())).thenReturn(popUserOTPEntity)
    Mockito.`when`(prisonerSearchApiService.findPrisonerPersonalDetails(prisoner.nomsId)).thenReturn(prisonerResponse)
    Mockito.lenient().`when`(prisonerSearchApiService.getPrisonerDetailsByNomsId(prisoner.nomsId)).thenReturn(prisonerSearch)
    Mockito.`when`(popUserApiService.postPoPUserVerification(oneLoginUserData, Optional.of(prisoner), prisonerResponse)).thenReturn(popUserResponse)
    val result = popUserOTPService.getPoPUserVerified(oneLoginUserData)
    Mockito.verify(popUserOTPRepository).delete(popUserOTPEntity)
    Assertions.assertEquals(popUserResponse, result)
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test generate otp is 6 digits and AlphaNumeric`() {
    repeat(1000) {
      val otpValue = randomAlphaNumericString()
      Assertions.assertEquals(otpValue.length, 6)
      for (ch in otpValue) {
        Assertions.assertEquals(ch.toString().matches("[a-zA-Z0-9]".toRegex()), true)
      }
    }
  }

  @Test
  fun `test create Pop User Verified - Fails OTP Expired`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val oneLoginData = OneLoginData("urn1", "123457", "email@test.com", LocalDate.parse("1982-10-24"))
    Mockito.lenient().`when`(popUserOTPRepository.findByOtpAndDobAndExpiryDateIsGreaterThan(oneLoginData.otp ?: "0", testDate.toLocalDate(), testDate)).thenReturn(null)
    assertThrows<ResourceNotFoundException> { popUserOTPService.getPoPUserVerified(oneLoginData) }
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test create Pop User Verified - Fails DOB not match`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val oneLoginData = OneLoginData("urn1", "123457", "email@test.com", LocalDate.parse("1982-10-24"))
    val dob = testDate.toLocalDate()
    Mockito.lenient().`when`(popUserOTPRepository.findByOtpAndDobAndExpiryDateIsGreaterThan(oneLoginData.otp ?: "0", dob, testDate)).thenReturn(null)
    assertThrows<ResourceNotFoundException> { popUserOTPService.getPoPUserVerified(oneLoginData) }
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test create Pop User Verified - User Service Response null`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val oneLoginUserData = OneLoginData("urn1", "123457", "email@test.com", LocalDate.parse("1982-10-24"))
    val prisoner = PrisonerEntity(1, "acb", fakeNow, "crn", "xyz", null)
    val prisonerResponse = PrisonersSearch(
      prisonerNumber = "A123456",
      firstName = "firstName",
      lastName = "lastName",
      prisonId = "MDI",
      prisonName = "prisonName",
      cellLocation = null,
      youthOffender = false,
      dateOfBirth = LocalDate.parse("1982-10-24"),
      releaseDate = testDate.toLocalDate().plusMonths(6),
    )
    val popUserOTPEntity = PoPUserOTPEntity(
      1,
      prisoner,
      fakeNow,
      fakeNow.plusDays(7).withHour(23).withMinute(59).withSecond(59),
      "123457",
      LocalDate.parse("1982-10-24"),
    )
    val prisonerPersonal = PrisonerPersonal(
      prisonerNumber = "A123456",
      firstName = "firstName",
      lastName = "lastName",
      prisonId = "MDI",
      age = 20,
      dateOfBirth = LocalDate.parse("1982-10-24"),
      releaseDate = testDate.toLocalDate().plusMonths(6),
    )
    val prisonerSearch = Prisoner(prisonerPersonal, null, false, false)

    Mockito.`when`(prisoner.id?.let { prisonerRepository.findById(it) }).thenReturn(Optional.of(prisoner))
    Mockito.lenient().`when`(popUserOTPRepository.findByOtpAndDobAndExpiryDateIsGreaterThan(oneLoginUserData.otp, LocalDate.parse("1982-10-24"), LocalDateTime.now())).thenReturn(popUserOTPEntity)
    Mockito.`when`(prisonerSearchApiService.findPrisonerPersonalDetails(prisoner.nomsId)).thenReturn(prisonerResponse)
    Mockito.lenient().`when`(prisonerSearchApiService.getPrisonerDetailsByNomsId(prisoner.nomsId)).thenReturn(prisonerSearch)
    Mockito.`when`(popUserApiService.postPoPUserVerification(oneLoginUserData, Optional.of(prisoner), prisonerResponse)).thenReturn(null)
    assertThrows<RuntimeException> { popUserOTPService.getPoPUserVerified(oneLoginUserData) }
    unmockkStatic(LocalDateTime::class)
  }
}
