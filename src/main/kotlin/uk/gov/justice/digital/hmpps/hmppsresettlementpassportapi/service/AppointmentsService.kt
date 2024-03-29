package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Address
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Appointment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.AppointmentsList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CreateAppointment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.AppointmentDelius
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ContactType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DeliusContactEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration

@Service
class AppointmentsService(
  private val prisonerRepository: PrisonerRepository,
  private val rpDeliusApiService: ResettlementPassportDeliusApiService,
  private val deliusContactService: DeliusContactService,
) {

  companion object {
    const val SECTION_DELIMITER = "###"
    private const val APPOINTMENT_TITLE = "Appointment Title"
    private const val CONTACT = "Contact"
    private const val ORGANISATION = "Organisation"
    private const val LOCATION = "Location"
    private const val BUILDING_NAME = "  Building Name"
    private const val BUILDING_NUMBER = "  Building Number"
    private const val STREET_NAME = "  Street Name"
    private const val DISTRICT = "  District"
    private const val TOWN = "  Town"
    private const val COUNTY = "  County"
    private const val POSTCODE = "  Postcode"
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun getAppointmentsById(
    nomsId: String,
    id: Long,
  ): Appointment {
    return mapAppointmentFromDatabase(deliusContactService.getAppointmentById(id, nomsId))
  }

  @Transactional
  fun getAppointmentsByNomsId(
    nomsId: String,
    startDate: LocalDate,
    endDate: LocalDate,
  ): AppointmentsList {
    if (nomsId.isBlank()) {
      throw NoDataWithCodeFoundException("Prisoner", nomsId)
    }

    val prisonerEntity = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val crn = prisonerEntity.crn ?: throw ResourceNotFoundException("Prisoner with id $nomsId has no CRN in database")

    val appointments = mutableListOf<Appointment>()
    appointments.addAll(mapAppointmentsFromDeliusApi(rpDeliusApiService.fetchAppointments(nomsId, crn, startDate, endDate)))
    appointments.addAll(mapAppointmentsFromDatabase(deliusContactService.getAppointments(nomsId)))

    appointments.sortBy { LocalDateTime.of(it.date, it.time) }

    return AppointmentsList(appointments)
  }

  private fun mapAppointmentsFromDeliusApi(appList: List<AppointmentDelius>): List<Appointment> {
    val appointmentList = mutableListOf<Appointment>()
    appList.forEach {
      val appointment: Appointment?
      val duration: Duration? = try {
        it.duration?.let { it1 -> Duration.parseIsoString(it1) }
      } catch (ex: IllegalArgumentException) {
        log.warn("Unable to parse the duration value  " + it.duration)
        null
      }

      val addressInfo: Address = if (it.location?.address != null) {
        Address(
          it.location.address.buildingName,
          it.location.address.buildingNumber,
          it.location.address.streetName,
          it.location.address.district,
          it.location.address.town,
          it.location.address.county,
          it.location.address.postcode,
          null,
        )
      } else {
        Address(null, null, null, null, null, null, null, it.location?.description)
      }

      var formattedDateVal: LocalDate? = null
      var formattedTimeVal: LocalTime? = null
      if (it.dateTime != null) {
        formattedDateVal = OffsetDateTime.parse(it.dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDate()
        formattedTimeVal = OffsetDateTime.parse(it.dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalTime()
      }

      appointment = Appointment(
        it.description,
        it.staff.name.forename + " " + it.staff.name.surname,
        formattedDateVal,
        formattedTimeVal,
        addressInfo,
        it.staff.email,
        duration?.inWholeMinutes,
      )
      appointmentList.add(appointment)
    }
    return appointmentList
  }

  fun mapAppointmentsFromDatabase(deliusContacts: List<DeliusContactEntity>) = deliusContacts.map { deliusContact ->
    mapAppointmentFromDatabase(deliusContact)
  }

  fun mapAppointmentFromDatabase(deliusContact: DeliusContactEntity): Appointment {
    val customFieldsFromNotes = getCustomFieldsFromNotes(deliusContact.notes, deliusContact.id)
    return Appointment(
      title = extractSectionFromNotes(customFieldsFromNotes, APPOINTMENT_TITLE, deliusContact.id),
      contact = extractSectionFromNotes(customFieldsFromNotes, CONTACT, deliusContact.id),
      date = deliusContact.appointmentDate?.toLocalDate(),
      time = deliusContact.appointmentDate?.toLocalTime(),
      location = Address(
        buildingName = extractSectionFromNotesTrimToNull(customFieldsFromNotes, BUILDING_NAME, deliusContact.id),
        buildingNumber = extractSectionFromNotesTrimToNull(customFieldsFromNotes, BUILDING_NUMBER, deliusContact.id),
        streetName = extractSectionFromNotesTrimToNull(customFieldsFromNotes, STREET_NAME, deliusContact.id),
        district = extractSectionFromNotesTrimToNull(customFieldsFromNotes, DISTRICT, deliusContact.id),
        town = extractSectionFromNotesTrimToNull(customFieldsFromNotes, TOWN, deliusContact.id),
        county = extractSectionFromNotesTrimToNull(customFieldsFromNotes, COUNTY, deliusContact.id),
        postcode = extractSectionFromNotesTrimToNull(customFieldsFromNotes, POSTCODE, deliusContact.id),
        description = null,
      ),
      contactEmail = null,
      duration = deliusContact.appointmentDuration?.toLong(),
      note = deliusContact.notes,
    )
  }

  @Transactional
  fun createAppointment(appointment: CreateAppointment, nomsId: String, auth: String): ResponseEntity<Void> {
    val now = LocalDateTime.now()
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

    val appointmentEntity = DeliusContactEntity(
      null,
      prisoner,
      category = appointment.appointmentType,
      contactType = ContactType.APPOINTMENT,
      createdDate = now,
      appointmentDate = appointment.dateAndTime,
      appointmentDuration = appointment.appointmentDuration,
      notes = createNotes(appointment),
      createdBy = getClaimFromJWTToken(auth, "name") ?: throw ServerWebInputException("Cannot get name from auth token"),
    )
    deliusContactService.addAppointmentToDatabase(appointmentEntity)
    return ResponseEntity.ok().build()
  }

  fun createNotes(appointment: CreateAppointment): String {
    return """
      $SECTION_DELIMITER
      $APPOINTMENT_TITLE: ${appointment.appointmentTitle}
      $CONTACT: ${appointment.contact}
      $ORGANISATION: ${appointment.organisation}
      $LOCATION:
      $BUILDING_NAME: ${appointment.location.buildingName ?: ""}
      $BUILDING_NUMBER: ${appointment.location.buildingNumber ?: ""}
      $STREET_NAME: ${appointment.location.streetName ?: ""}
      $DISTRICT: ${appointment.location.district ?: ""}
      $TOWN: ${appointment.location.town ?: ""}
      $COUNTY: ${appointment.location.county ?: ""}
      $POSTCODE: ${appointment.location.postcode ?: ""}
      $SECTION_DELIMITER
      ${appointment.notes ?: ""}
      $SECTION_DELIMITER
    """.trimIndent()
  }
}
