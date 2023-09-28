package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

data class Accommodation(
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
