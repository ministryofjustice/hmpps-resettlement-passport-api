package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
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

@Service
class AppointmentsService(
  private val prisonerRepository: PrisonerRepository,
  private val rpDeliusApiService: ResettlementPassportDeliusApiService,
  private val deliusContactService: DeliusContactService,

) {
  @Transactional
  fun getAppointmentsByNomsId(
    nomsId: String,
    startDate: LocalDate,
    endDate: LocalDate,
    pageNumber: Int,
    pageSize: Int,
  ): AppointmentsList {
    if (nomsId.isBlank()) {
      throw NoDataWithCodeFoundException("Prisoner", nomsId)
    }

    if (pageNumber < 0 || pageSize <= 0) {
      throw NoDataWithCodeFoundException(
        "Data",
        "Page $pageNumber and Size $pageSize",
      )
    }

    val prisonerEntity = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val crn = prisonerEntity.crn ?: throw ResourceNotFoundException("Prisoner with id $nomsId has no CRN in database")

    val deliusAppointments = rpDeliusApiService.fetchAppointments(nomsId, crn, startDate, endDate, pageNumber, pageSize)
    val appList: List<Appointment> = objectMapper(deliusAppointments.results)

    return AppointmentsList(
      appList,
      deliusAppointments.totalElements,
      deliusAppointments.totalPages,
      deliusAppointments.page,
      deliusAppointments.size,
    )
  }

  private fun objectMapper(appList: List<AppointmentDelius>): List<Appointment> {
    val appointmentList = mutableListOf<Appointment>()
    appList.forEach {
      val appointment: Appointment?
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
      )
      appointmentList.add(appointment)
    }
    return appointmentList
  }

  @Transactional
  fun createAppointment(appointment: CreateAppointment, nomsId: String, auth: String): ResponseEntity<Void> {
    val now = LocalDateTime.now()
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val statusText = "Pending"

    val appointmentEntity = DeliusContactEntity(
      null,
      prisoner,
      category = appointment.appointmentType,
      contactType = ContactType.APPOINTMENT,
      createdDate = now,
      appointmentDate = appointment.dateAndTime,
      appointmentDuration = appointment.appointmentDuration,
      notes = appointment.notes ?: "",
      createdBy = getClaimFromJWTToken(auth, "name") ?: throw ServerWebInputException("Cannot get name from auth token")
    )
    deliusContactService.addAppointmentToDatabase(appointmentEntity)
    return ResponseEntity.ok().build()
  }

  fun createNotes(appointment: CreateAppointment): String {
    return "###\nAppointment Title: ${appointment.appointmentTitle}\nContact: ${appointment.contact}\nOrganisation: ${appointment.organisation}\nLocation:\n  Building Name: ${appointment.location.buildingName}\n  " +
      "Building Number: ${appointment.location.buildingNumber}\n  Street Name: ${appointment.location.streetName}\n  " +
      "District: ${appointment.location.district}\n  Town: ${appointment.location.town}\n  " +
      "County: ${appointment.location.county}\n  Postcode: ${appointment.location.postcode}\n###\n${appointment.notes}\n###"
  }
}
