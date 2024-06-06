package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Address
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Appointment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.AppointmentsList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CreateAppointment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.AppointmentDelius
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.DeliusCreateAppointment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi.DeliusCreateAppointmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.interventionsapi.CRSAppointmentsDTO
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.interventionsapi.ReferralAppointment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.InterventionsApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlin.time.toJavaDuration

@Service
class AppointmentsService(
  private val prisonerRepository: PrisonerRepository,
  private val rpDeliusApiService: ResettlementPassportDeliusApiService,
  private val interventionsApiService: InterventionsApiService,
  @Value("\${interventions-api-integration.crsAppointmentsEnabled}") private val crsAppointmentIntegrationEnabled: Boolean,
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
    private const val CRS_APPOINTMENT_DEFAULT_CONTACT = "Not provided"
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Transactional
  fun getAppointmentsByNomsId(
    nomsId: String,
    startDate: LocalDate,
    endDate: LocalDate,
    includePreRelease: Boolean,
  ): AppointmentsList {
    if (nomsId.isBlank()) {
      throw NoDataWithCodeFoundException("Prisoner", nomsId)
    }

    val prisonerEntity = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val crn = prisonerEntity.crn ?: throw ResourceNotFoundException("Prisoner with id $nomsId has no CRN in database")
    val crsAppointments: CRSAppointmentsDTO
    var deliusAppointments = rpDeliusApiService.fetchAppointments(nomsId, crn, startDate, endDate)

    if (crsAppointmentIntegrationEnabled) {
      crsAppointments = interventionsApiService.fetchCRSAppointments(crn)
      deliusAppointments = deliusAppointments.filter { it -> !it.description.contains("Appointment with CRS") }
    } else {
      crsAppointments = CRSAppointmentsDTO(crn, listOf<ReferralAppointment>())
    }
    val appointments = mapAppointmentsFromDeliusAndInterventionsApi(deliusAppointments, crsAppointments, startDate, endDate)

    return AppointmentsList(filterPreReleaseAppointments(appointments, prisonerEntity, includePreRelease).sortedBy { LocalDateTime.of(it.date, it.time) })
  }

  private fun filterPreReleaseAppointments(appointments: List<Appointment>, prisonerEntity: PrisonerEntity, includePreRelease: Boolean): List<Appointment> {
    return if (!includePreRelease && prisonerEntity.releaseDate != null) {
      appointments.filter { prisonerEntity.releaseDate!! <= it.date }
    } else {
      appointments
    }
  }

  private fun mapAppointmentsFromDeliusAndInterventionsApi(appList: List<AppointmentDelius>, crsAppointments: CRSAppointmentsDTO, startDate: LocalDate, endDate: LocalDate): List<Appointment> {
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
      appointment.type = it.type.code
      appointmentList.add(appointment)
    }

    var referrals = listOf<ReferralAppointment>()
    if (crsAppointments.referral != null) {
      referrals = crsAppointments.referral
    }

    referrals.forEach { referral ->
      val referralAppointments = referral.appointment
      referralAppointments.forEach { crsAppointment ->
        var formattedDateVal: LocalDate? = null
        var formattedTimeVal: LocalTime? = null
        if (crsAppointment.appointmentDateTime != null) {
          formattedDateVal =
            OffsetDateTime.parse(crsAppointment.appointmentDateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
              .toLocalDate()
          formattedTimeVal =
            OffsetDateTime.parse(crsAppointment.appointmentDateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
              .toLocalTime()
        }
        if (formattedDateVal?.isAfter(startDate.minusDays(1)) == true && formattedDateVal?.isBefore(endDate.plusDays(1)) == true) {
          val appointment: Appointment?
          val addressInfo = Address(
            null,
            crsAppointment.appointmentDeliveryFirstAddressLine,
            crsAppointment.appointmentDeliverySecondAddressLine,
            null,
            crsAppointment.appointmentDeliveryTownCity,
            crsAppointment.appointmentDeliveryCounty,
            crsAppointment.appointmentDeliveryPostCode,
            null,
          )

          appointment = Appointment(
            referral.interventionTitle,
            CRS_APPOINTMENT_DEFAULT_CONTACT,
            formattedDateVal,
            formattedTimeVal,
            addressInfo,
            null,
            crsAppointment.appointmentDurationInMinutes.toLong(),
          )
          appointment.type = ""
          appointmentList.add(appointment)
        }
      }
    }
    return appointmentList
  }

  @Transactional
  fun createAppointment(appointment: CreateAppointment, nomsId: String, auth: String): ResponseEntity<Void> {
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")

    rpDeliusApiService.createAppointment(
      prisoner.crn!!,
      DeliusCreateAppointment(
        type = DeliusCreateAppointmentType.fromCategory(appointment.appointmentType),
        start = appointment.dateAndTime.atZone(ZoneId.of("Europe/London")),
        duration = appointment.appointmentDuration.toDuration(DurationUnit.MINUTES).toJavaDuration(),
        notes = createNotes(appointment),
      ),
    )
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
