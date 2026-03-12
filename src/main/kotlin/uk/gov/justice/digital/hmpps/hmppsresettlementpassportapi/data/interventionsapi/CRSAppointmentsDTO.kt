package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.interventionsapi

import com.fasterxml.jackson.annotation.JsonProperty

data class CRSAppointmentsDTO(
  val crn: String,
  val referral: List<ReferralAppointment>?,

)

data class ReferralAppointment(
  @param:JsonProperty("referral_number")
  val referralNumber: String,
  @param:JsonProperty("intervention_title")
  val interventionTitle: String,
  @param:JsonProperty("appointment")
  val appointment: List<CRSAppointment>,
)

data class CRSAppointment(
  @param:JsonProperty("appointment_id")
  val appointmentId: String,
  @param:JsonProperty("appointment_date_time")
  val appointmentDateTime: String?,
  @param:JsonProperty("appointment_duration_in_minutes")
  val appointmentDurationInMinutes: Int,
  @param:JsonProperty("superseded_indicator")
  val supersededIndicator: Boolean,
  @param:JsonProperty("appointment_delivery_first_address_line")
  val appointmentDeliveryFirstAddressLine: String,
  @param:JsonProperty("appointment_delivery_second_address_line")
  val appointmentDeliverySecondAddressLine: String,
  @param:JsonProperty("appointment_delivery_town_city")
  val appointmentDeliveryTownCity: String,
  @param:JsonProperty("appointment_delivery_county")
  val appointmentDeliveryCounty: String,
  @param:JsonProperty("appointment_delivery_postcode")
  val appointmentDeliveryPostCode: String,

)
