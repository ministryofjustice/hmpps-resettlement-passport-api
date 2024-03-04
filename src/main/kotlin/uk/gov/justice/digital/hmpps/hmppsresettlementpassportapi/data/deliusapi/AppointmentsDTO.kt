package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi

data class AppointmentDelius(
  val type: Info,
  val dateTime: String?,
  val duration: DurationInfo?,
  val staff: StaffInfo,
  val location: LocationInfo?,
  val description: String,
  val outcome: Info?,
)

data class Info(
  val code: String,
  val description: String?,
)

data class LocationInfo(
  val code: String,
  val description: String?,
  val address: Address?,
)

data class DurationInfo(
  val seconds: Long,
  val zero: Boolean,
  val nano: Long?,
  val negative: Boolean,
  val positive: Boolean,
  val units: List<UnitsInfo> = listOf(),
)

data class UnitsInfo(
  val durationEstimated: Boolean,
  val timeBased: Boolean,
  val dateBased: Boolean,
)

data class Address(
  val buildingName: String?,
  val buildingNumber: String?,
  val streetName: String?,
  val district: String?,
  val town: String?,
  val county: String?,
  val postcode: String?,
)
data class StaffInfo(
  val code: String,
  val name: Fullname,
  val email: String,
)

data class Fullname(
  val forename: String,
  val surname: String,
)

data class AppointmentsDeliusList(
  val results: List<AppointmentDelius> = listOf(),
  val totalElements: Int,
  val totalPages: Int,
  val page: Int,
  val size: Int,
)
