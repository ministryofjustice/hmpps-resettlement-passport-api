package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

data class Accommodation(
  val mainAddress: String?,
  val message: String?
)

data class OfficerInfo(
  val forename: String?,
  val surname: String?,
  val middlename: String?,
)
