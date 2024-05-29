package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.interventionsapi

import com.fasterxml.jackson.annotation.JsonProperty

data class CRSAppointmentsDTO(
  val crn: String,
  val referral: List<ReferralAppointment>,

)

data class ReferralAppointment(
  @JsonProperty("referral_number")
  val referralNumber: String,
  @JsonProperty("appointment")
  val appointment: List<CRSAppointment>
)

data class CRSAppointment(
  @JsonProperty("appointment_id")
  val appointmentId: String,
  @JsonProperty("appointment_date_time")
  val appointmentDateTime: String?,
  @JsonProperty("appointment_duration_in_minutes")
  val appointmentDurationInMinutes: Int,
  @JsonProperty("superseded_indicator")
  val supersededIndicator: Boolean,
  @JsonProperty("appointment_delivery_first_address_line")
  val appointmentDeliveryFirstAddressLine: String,
  @JsonProperty("appointment_delivery_second_address_line")
  val appointmentDeliverySecondAddressLine: String,
  @JsonProperty("appointment_delivery_town_city")
  val appointmentDeliveryTownCity: String,
  @JsonProperty("appointment_delivery_county")
  val appointmentDeliveryCounty: String,
  @JsonProperty("appointment_delivery_postcode")
  val appointmentDeliveryPostCode: String,

  )
