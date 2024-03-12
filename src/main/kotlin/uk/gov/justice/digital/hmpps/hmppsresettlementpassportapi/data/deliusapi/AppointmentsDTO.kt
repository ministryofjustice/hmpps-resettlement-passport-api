package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi

data class AppointmentDelius(
  val type: Info,
  val dateTime: String?,
  val duration: String?,
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
