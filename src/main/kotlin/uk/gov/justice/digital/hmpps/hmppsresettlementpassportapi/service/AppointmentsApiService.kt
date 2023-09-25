package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Address
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Appointment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.AppointmentsList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.AppointmentDelius
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Service
class AppointmentsApiService(
  private val prisonerRepository: PrisonerRepository,
  private val rpDeliusApiService: ResettlementPassportDeliusApiService,
) {

  suspend fun getAppointmentsByNomisId(
    nomisId: String,
    startDate: LocalDate,
    endDate: LocalDate,
    pageNumber: Int,
    pageSize: Int,
  ): AppointmentsList {
    if (nomisId.isBlank() || nomisId.isEmpty()) {
      throw NoDataWithCodeFoundException("Prisoner", nomisId)
    }

    if (pageNumber < 0 || pageSize <= 0) {
      throw NoDataWithCodeFoundException(
        "Data",
        "Page $pageNumber and Size $pageSize",
      )
    }
    val prisonerEntity = prisonerRepository.findByNomsId(nomisId) ?: throw ResourceNotFoundException("Prisoner with id $nomisId not found in database")

    var crn = prisonerEntity.crn

    crn = if (crn.isBlank() or crn.isEmpty()) {
      rpDeliuseApiService.getCrn(nomisId).toString()
    } else {
      prisonerEntity.crn
    }

    var appointmentList = AppointmentsList(listOf(), 0, 0, 0, 0)

    val deliusAppointments = rpDeliuseApiService.fetchAppointments(crn, startDate, endDate, pageNumber, pageSize)
    deliusAppointments.collect {
      val appList: List<Appointment> = objectMapper(it.results)
      appointmentList = AppointmentsList(appList, it.totalElements, it.totalPages, it.page, it.size)
    }

    return appointmentList
  }

  private suspend fun objectMapper(appList: List<AppointmentDelius>): List<Appointment> {
    val appointmentList = mutableListOf<Appointment>()
    appList.forEach {
      var addressInfo: Address? = null
      val appointment: Appointment?
      if (it.location?.address != null) {
        addressInfo = Address(
          it.location.address.buildingName,
          it.location.address.buildingNumber,
          it.location.address.streetName,
          it.location.address.district,
          it.location.address.town,
          it.location.address.county,
          it.location.address.postcode,
        )
      }
      val formattedDateTime = OffsetDateTime.parse(it.dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
      appointment = if (addressInfo != null) {
        Appointment(
          it.description,
          it.staff.name.forename + " " + it.staff.name.surname,
          formattedDateTime.toLocalDate(),
          formattedDateTime.toLocalTime(),
          addressInfo,
        )
      } else {
        Appointment(
          it.description,
          it.staff.name.forename + " " + it.staff.name.surname,
          formattedDateTime.toLocalDate(),
          formattedDateTime.toLocalTime(),
          null,
        )
      }
      appointmentList.add(appointment)
    }
    return appointmentList
  }
}
