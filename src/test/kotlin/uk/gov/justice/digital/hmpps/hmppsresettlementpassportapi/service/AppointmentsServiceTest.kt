package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CreateAppointment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CreateAppointmentAddress
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Category
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class AppointmentsServiceTest {

  private lateinit var appointmentsService: AppointmentsService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var rpDeliusApiService: ResettlementPassportDeliusApiService

  @Mock
  private lateinit var deliusContactService: DeliusContactService

  @BeforeEach
  fun beforeEach() {
    appointmentsService = AppointmentsService(prisonerRepository, rpDeliusApiService, deliusContactService)
  }

  @Test
  fun createFieldsFromNotes() {
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
