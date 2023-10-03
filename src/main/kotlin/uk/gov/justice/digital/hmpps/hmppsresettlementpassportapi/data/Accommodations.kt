package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

data class Accommodation(
  val referralDate: String?,
  val provider: String?,
  val team: String?,
  val officer: OfficerInfo?,
  val status: String?,
  val startDateTime: String?,
  val notes: String?,
  val mainAddress: AddressInfo,
)

data class AddressInfo(
  val buildingName: String?,
  val buildingNumber: String?,
  val streetName: String?,
  val district: String?,
  val town: String?,
  val county: String?,
  val postcode: String?,
  val message: String?,
)

data class OfficerInfo(
  val forename: String?,
  val surname: String?,
  val middlename: String?,
)
