package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

data class PopUserCountMetrics(
  val metrics: MutableMap<Prison, List<PopUserLicenceCountMetric>> = mutableMapOf(),
)

enum class LicenceTag(val label: String) {
  MISSING_STANDARD_PERCENTAGE("Standard Percentage"),
  MISSING_OTHERS_PERCENTAGE("Others Percentage"),
  MISSING_STANDARD_COUNT("Standard Count"),
  MISSING_OTHERS_COUNT("Others Count"),
}
data class PopUserLicenceCountMetric(
  val licenceType: LicenceTag,
  val value: Double,
)

enum class MissingFieldScore(val score: Int) {
  TWO(2),
  ONE(1),
}

enum class AppointmentsDataTag(val label: String) {
  MISSING_DATE_SCORE("Date Score"),
  MISSING_TIME_SCORE("Time Score"),
  MISSING_TYPE_SCORE("Type Score"),
  MISSING_LOCATION_SCORE("Location Score"),
  MISSING_PROBATION_OFFICER_SCORE("Probation Officer Score"),
  MISSING_EMAIL_SCORE("Email Score"),
  MISSING_DATE_PERCENTAGE("Date Percentage"),
  MISSING_TIME_PERCENTAGE("Time Percentage"),
  MISSING_TYPE_PERCENTAGE("Type Percentage"),
  MISSING_LOCATION_PERCENTAGE("Location Percentage"),
  MISSING_PROBATION_OFFICER_PERCENTAGE("Probation Officer Percentage"),
  MISSING_EMAIL_PERCENTAGE("Email Percentage"),
  MISSING_DATE_COUNT("Date Count"),
  MISSING_TIME_COUNT("Time Count"),
  MISSING_TYPE_COUNT("Type Count"),
  MISSING_LOCATION_COUNT("Location Count"),
  MISSING_PROBATION_OFFICER_COUNT("Probation Officer Count"),
  MISSING_EMAIL_COUNT("Email Count"),
  RELEASE_DAY_ZERO_COUNT("No Appointments Count"),
  RELEASE_DAY_PROBATION_ZERO_COUNT("No Probation Appointments Count"),
}

data class PopUserAppointmentCountMetric(
  val appointmentFieldType: AppointmentsDataTag,
  val value: Double,
)

data class PopUserAppointmentCountMetrics(
  val metrics: MutableMap<Prison, List<PopUserAppointmentCountMetric>> = mutableMapOf(),
)
