package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PoPUserResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.popuserapi.KnowledgeBasedVerification
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.popuserapi.OneLoginData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi.PrisonersSearch
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PoPUserOTPEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PoPUserOTPRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PoPUserApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
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

  @Mock
  private lateinit var prisonerSearchApiService: PrisonerSearchApiService

  @Mock
  private lateinit var resettlementPassportDeliusApiService: ResettlementPassportDeliusApiService

  private val testDate = LocalDateTime.parse("2023-08-25T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    popUserOTPService =
      PoPUserOTPService(popUserOTPRepository, prisonerRepository, popUserApiService, prisonerSearchApiService, resettlementPassportDeliusApiService)
  }

  @Test
  fun `test get PoP User OTP - returns PoP User OTP `() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "xyz")
    val popUserOTPEntity = PoPUserOTPEntity(
      null,
      prisonerEntity.id(),
      fakeNow,
      fakeNow.plusDays(7).withHour(11).withMinute(59).withSecond(59),
      "1X3456",
      LocalDate.parse("1982-10-24"),
    )

    whenever(popUserOTPRepository.findByPrisonerId(any())).thenReturn(popUserOTPEntity)
    val result = popUserOTPService.getOTPByPrisoner(prisonerEntity)
    Assertions.assertEquals(popUserOTPEntity.id, result!!.id)
  }

  @Test
  fun `test get PoP User OTP - returns PoP User not found `() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "xyz")

    whenever(popUserOTPRepository.findByPrisonerId(any())).thenReturn(null)
    val thrown = assertThrows<ResourceNotFoundException> { popUserOTPService.getOTPByPrisoner(prisonerEntity) }
    Assertions.assertEquals("OTP for Prisoner with id 1 not found in database", thrown.message)
  }

  @Test
  fun `test delete PoPUserOTP - hard delete`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "xyz")
    val popUserOTPEntity = PoPUserOTPEntity(
      null,
      prisonerEntity.id(),
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
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "xyz")
    val popUserOTPEntity = PoPUserOTPEntity(
      null,
      prisonerEntity.id(),
      fakeNow,
      fakeNow.plusDays(7).withHour(11).withMinute(59).withSecond(59),
      "1X3456",
      LocalDate.parse("1982-10-24"),
    )

    val popUserOTPList = emptyList<PoPUserOTPEntity>().toMutableList()
    popUserOTPList.add(popUserOTPEntity)

    whenever(popUserOTPRepository.findAll()).thenReturn(popUserOTPList)
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
    val prisonerEntity = PrisonerEntity(1, "acb", fakeNow, "xyz")
    val popUserOTPEntity = PoPUserOTPEntity(
      null,
      prisonerEntity.id(),
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

    whenever(popUserOTPRepository.findByPrisonerId(prisonerEntity.id())).thenReturn(null)
    whenever(popUserOTPRepository.save(any())).thenReturn(popUserOTPEntity)
    whenever(prisonerSearchApiService.findPrisonerPersonalDetails(prisonerEntity.nomsId)).thenReturn(prisonerResponse)
    val result = popUserOTPService.createPoPUserOTP(prisonerEntity)
    Assertions.assertEquals(popUserOTPEntity.id, result.id)
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test create Pop User Verified - Fails OTP Invalid`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val oneLoginData = OneLoginData("urn1", "123457", "email@test.com", LocalDate.parse("1982-10-24"))
    whenever(
      popUserOTPRepository.findByOtpAndDobAndExpiryDateIsGreaterThan(
        oneLoginData.otp,
        LocalDate.parse("1982-10-24"),
        LocalDateTime.now(),
      ),
    ).thenReturn(null)
    val thrown = assertThrows<ResourceNotFoundException> { popUserOTPService.getPoPUserVerified(oneLoginData) }
    Assertions.assertEquals("Person On Probation User otp  123457  not found in database or expired.", thrown.message)
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test create Pop User Verified -  valid OTP`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val oneLoginUserData = OneLoginData("urn1", "123457", "email@test.com", LocalDate.parse("1982-10-24"))
    val prisoner = PrisonerEntity(1, "acb", fakeNow, "xyz")
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
      prisoner.id(),
      fakeNow,
      fakeNow.plusDays(7).withHour(23).withMinute(59).withSecond(59),
      "123457",
      LocalDate.parse("1982-10-24"),
    )

    whenever(prisoner.id?.let { prisonerRepository.findById(it) }).thenReturn(Optional.of(prisoner))
    whenever(
      popUserOTPRepository.findByOtpAndDobAndExpiryDateIsGreaterThan(
        oneLoginUserData.otp,
        LocalDate.parse("1982-10-24"),
        LocalDateTime.now(),
      ),
    ).thenReturn(popUserOTPEntity)
    whenever(prisonerSearchApiService.findPrisonerPersonalDetails(prisoner.nomsId)).thenReturn(prisonerResponse)
    whenever(resettlementPassportDeliusApiService.getCrn(prisoner.nomsId)).thenReturn("crn")
    whenever(popUserApiService.postPoPUserVerification(oneLoginUserData.urn, prisoner.nomsId, "crn")).thenReturn(popUserResponse)
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
    Mockito.lenient().`when`(
      popUserOTPRepository.findByOtpAndDobAndExpiryDateIsGreaterThan(
        oneLoginData.otp,
        testDate.toLocalDate(),
        testDate,
      ),
    ).thenReturn(null)
    assertThrows<ResourceNotFoundException> { popUserOTPService.getPoPUserVerified(oneLoginData) }
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test create Pop User Verified - Fails DOB not match`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val oneLoginData = OneLoginData("urn1", "123457", "email@test.com", LocalDate.parse("1982-10-24"))
    val dob = testDate.toLocalDate()
    Mockito.lenient()
      .`when`(popUserOTPRepository.findByOtpAndDobAndExpiryDateIsGreaterThan(oneLoginData.otp ?: "0", dob, testDate))
      .thenReturn(null)
    assertThrows<ResourceNotFoundException> { popUserOTPService.getPoPUserVerified(oneLoginData) }
    unmockkStatic(LocalDateTime::class)
  }

  @Test
  fun `test getAll PoP Verified User  - returns PoP Verified User  List`() {
    val prisonerEntity = PrisonerEntity(1, "acb", testDate, "xyz")
    val popUserOTPEntity = PoPUserOTPEntity(
      null,
      prisonerEntity.id(),
      fakeNow,
      fakeNow.plusDays(7).withHour(11).withMinute(59).withSecond(59),
      "1X3456",
      LocalDate.parse("1982-10-24"),
    )

    val popUserOTPList = emptyList<PoPUserOTPEntity>().toMutableList()
    popUserOTPList.add(popUserOTPEntity)

    whenever(popUserOTPRepository.findAll()).thenReturn(popUserOTPList)
    val result = popUserOTPService.getAllOTPs()
    Assertions.assertEquals(popUserOTPList, result)
  }

  @Test
  fun `matches knowledge answers to search response - match`() {
    val formData = KnowledgeBasedVerification(
      firstName = "firstName",
      lastName = "lastName",
      dateOfBirth = LocalDate.parse("1982-10-24"),
      urn = "urn",
      email = "email@test.com",
      nomsId = "noms1",
    )
    val searchResponse = PrisonersSearch(
      prisonerNumber = "noms1",
      firstName = "FIRSTNAME",
      lastName = "LASTNAME",
      dateOfBirth = LocalDate.parse("1982-10-24"),
      prisonId = "prisonId",
      prisonName = "prisonName",
    )

    assertThat(exactlyMatching(formData)(searchResponse)).isTrue()
  }

  @Test
  fun `matches knowledge answers to search response - date of birth no match`() {
    val formData = KnowledgeBasedVerification(
      firstName = "firstName",
      lastName = "lastName",
      dateOfBirth = LocalDate.parse("1982-10-24"),
      urn = "urn",
      email = "email@test.com",
      nomsId = "noms1",
    )
    val searchResponse = PrisonersSearch(
      prisonerNumber = "noms1",
      firstName = "FIRSTNAME",
      lastName = "LASTNAME",
      dateOfBirth = LocalDate.parse("1982-10-25"),
      prisonId = "prisonId",
      prisonName = "prisonName",
    )

    assertThat(exactlyMatching(formData)(searchResponse)).isFalse()
  }

  @Test
  fun `matches knowledge answers to search response - nomis id no match`() {
    val formData = KnowledgeBasedVerification(
      firstName = "firstName",
      lastName = "lastName",
      dateOfBirth = LocalDate.parse("1982-10-24"),
      urn = "urn",
      email = "email@test.com",
      nomsId = "noms2",
    )
    val searchResponse = PrisonersSearch(
      prisonerNumber = "noms1",
      firstName = "FIRSTNAME",
      lastName = "LASTNAME",
      dateOfBirth = LocalDate.parse("1982-10-24"),
      prisonId = "prisonId",
      prisonName = "prisonName",
    )

    assertThat(exactlyMatching(formData)(searchResponse)).isFalse()
  }
}
