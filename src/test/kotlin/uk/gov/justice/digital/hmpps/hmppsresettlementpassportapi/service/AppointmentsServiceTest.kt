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

@ExtendWith(MockitoExtension::class)
class AppointmentsServiceTest {

  private lateinit var appointmentsService: AppointmentsService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var rpDeliusApiService: ResettlementPassportDeliusApiService

  private val nomisId = "ABC123"

  @BeforeEach
  fun beforeEach() {
    appointmentsService = AppointmentsService(prisonerRepository, rpDeliusApiService)
  }

  @Test
  fun `getAppointmentsByNomsId gets all appointments including preRelease`() {
    val prisonerEntity = PrisonerEntity(1, nomisId, LocalDateTime.MIN, "crn", "xyz", LocalDate.MIN)
    Mockito.`when`(prisonerRepository.findByNomsId(nomisId)).thenReturn(prisonerEntity)
    Mockito.`when`(rpDeliusApiService.fetchAppointments(eq(nomisId), any(), any(), any())).thenReturn(listOf(createTestDeliusAppointment()))

    val appointments = appointmentsService.getAppointmentsByNomsId(nomisId, LocalDate.MIN, LocalDate.MAX, true)
    Assertions.assertTrue(appointments.results.size == 1)
  }

  @Test
  fun `getAppointmentsByNomsId gets all appointments excluding preRelease`() {
    val prisonerEntity = PrisonerEntity(1, nomisId, LocalDateTime.MIN, "crn", "xyz", LocalDate.MAX)
    Mockito.`when`(prisonerRepository.findByNomsId(nomisId)).thenReturn(prisonerEntity)
    Mockito.`when`(rpDeliusApiService.fetchAppointments(eq(nomisId), any(), any(), any())).thenReturn(listOf(createTestDeliusAppointment()))

    val appointments = appointmentsService.getAppointmentsByNomsId(nomisId, LocalDate.MIN, LocalDate.MAX, false)
    Assertions.assertTrue(appointments.results.isEmpty())
  }

  @Test
  fun `getAppointmentsByNomsId gets all appointments handles null releaseDate excluding preRelease`() {
    val prisonerEntity = PrisonerEntity(1, nomisId, LocalDateTime.MIN, "crn", "xyz", null)
    Mockito.`when`(prisonerRepository.findByNomsId(nomisId)).thenReturn(prisonerEntity)
    Mockito.`when`(rpDeliusApiService.fetchAppointments(eq(nomisId), any(), any(), any())).thenReturn(listOf(createTestDeliusAppointment()))

    val appointments = appointmentsService.getAppointmentsByNomsId(nomisId, LocalDate.MIN, LocalDate.MAX, false)
    Assertions.assertTrue(appointments.results.size == 1)
  }

  @Test
  fun `getAppointmentsByNomsId gets all appointments sorted by date`() {
    val appointment1 = createTestDeliusAppointment("2024-05-02T12:00:00Z")
    val appointment2 = createTestDeliusAppointment("2024-05-01T10:00:00Z")
    val appointment3 = createTestDeliusAppointment("2024-05-01T15:00:00Z")
    val prisonerEntity = PrisonerEntity(1, nomisId, LocalDateTime.MIN, "crn", "xyz", LocalDate.MIN)
    Mockito.`when`(prisonerRepository.findByNomsId(nomisId)).thenReturn(prisonerEntity)
    Mockito.`when`(rpDeliusApiService.fetchAppointments(eq(nomisId), any(), any(), any())).thenReturn(listOf(appointment1, appointment2, appointment3))

    val appointments = appointmentsService.getAppointmentsByNomsId(nomisId, LocalDate.MIN, LocalDate.MAX, true)
    Assertions.assertTrue(appointments.results.size == 3)
    Assertions.assertEquals("2024-05-01", appointments.results[0].date.toString())
    Assertions.assertEquals("10:00", appointments.results[0].time.toString())
    Assertions.assertEquals("2024-05-01", appointments.results[1].date.toString())
    Assertions.assertEquals("15:00", appointments.results[1].time.toString())
    Assertions.assertEquals("2024-05-02", appointments.results[2].date.toString())
    Assertions.assertEquals("12:00", appointments.results[2].time.toString())
  }

  @Test
  fun `getAppointmentsByNomsId filter all appointments prior to release date`() {
    val appointmentAfterRelease = createTestDeliusAppointment("2024-05-03T12:00:00Z")
    val appointmentBeforeRelease1 = createTestDeliusAppointment("2024-05-01T10:00:00Z")
    val appointmentBeforeRelease2 = createTestDeliusAppointment("2024-05-01T15:00:00Z")
    val prisonerEntity = PrisonerEntity(1, nomisId, LocalDateTime.MIN, "crn", "xyz", LocalDate.of(2024, 5, 2))
    Mockito.`when`(prisonerRepository.findByNomsId(nomisId)).thenReturn(prisonerEntity)
    Mockito.`when`(rpDeliusApiService.fetchAppointments(eq(nomisId), any(), any(), any())).thenReturn(listOf(appointmentAfterRelease, appointmentBeforeRelease1, appointmentBeforeRelease2))

    val appointments = appointmentsService.getAppointmentsByNomsId(nomisId, LocalDate.MIN, LocalDate.MAX, false)
    Assertions.assertTrue(appointments.results.size == 1)
    Assertions.assertEquals("2024-05-03", appointments.results[0].date.toString())
    Assertions.assertEquals("12:00", appointments.results[0].time.toString())
  }

  @Test
  fun createFieldsFromNotes() {
    val testAppointment = createTestAppointment()
    val parsedNotes =
      """
       ###
       Appointment Title: rehab
       Contact: Emily
       Organisation: AA
       Location:
         Building Name: 
         Building Number: 8
         Street Name: Hayes Court
         District: 
         Town: Huddersfield
         County: West Yorkshire
         Postcode: HD1 4ST
       ###
       No notes
       ###
      """.trimIndent()
    val response = appointmentsService.createNotes(testAppointment)
    Assertions.assertEquals(parsedNotes, response)
  }

  private fun createTestDeliusAppointment(appointmentDate: String = "2024-05-02T10:00:00Z"): AppointmentDelius {
    val info = Info(code = "APPOINTMENT_TYPE_CODE", description = "Appointment Type Description")
    val staffInfo = StaffInfo(code = "STAFF_CODE", name = Fullname(forename = "John", surname = "Doe"), email = "john.doe@example.com")
    val locationInfo = LocationInfo(code = "LOCATION_CODE", description = "Location Description", address = Address(buildingName = "Building Name", buildingNumber = "123", streetName = "Street Name", district = "District", town = "Town", county = "County", postcode = "Postcode"))
    return AppointmentDelius(
      type = info,
      dateTime = appointmentDate,
      duration = "60",
      staff = staffInfo,
      location = locationInfo,
      description = "This is a sample appointment description.",
      outcome = null,
    )
  }

  private fun createTestAppointment(): CreateAppointment {
    val testLocation = CreateAppointmentAddress(
      "",
      "8",
      "Hayes Court",
      "",
      "Huddersfield",
      "West Yorkshire",
      "HD1 4ST",
    )
    val testAppointment = CreateAppointment(
      Category.DRUGS_AND_ALCOHOL,
      "rehab",
      "AA",
      "Emily",
      testLocation,
      LocalDateTime.parse("2023-12-14T18:13:00"),
      90,
      "No notes",
    )
    return testAppointment
  }

  @Test
  fun `test createFieldsFromNotes - null case`() {
    val testLocation = CreateAppointmentAddress(
      null,
      null,
      null,
      null,
      null,
      null,
      null,
    )
    val testAppointment = CreateAppointment(
      Category.DRUGS_AND_ALCOHOL,
      "rehab",
      "AA",
      "Emily",
      testLocation,
      LocalDateTime.parse("2023-12-14T18:13:00"),
      90,
      null,
    )
    val expectedNotes =
      """
       ###
       Appointment Title: rehab
       Contact: Emily
       Organisation: AA
       Location:
         Building Name: 
         Building Number: 
         Street Name: 
         District: 
         Town: 
         County: 
         Postcode: 
       ###
       
       ###
      """.trimIndent()
    val actualNotes = appointmentsService.createNotes(testAppointment)
    Assertions.assertEquals(expectedNotes, actualNotes)
  }
}
