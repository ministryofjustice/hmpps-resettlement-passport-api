package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi

import java.time.LocalDate
import java.time.ZonedDateTime

data class AccommodationsDelius(
  val nsiSubType: String,
  val referralDate: LocalDate?,
  val provider: String?,
  val team: String?,
  val officer: OfficerInfo?,
  val status: String?,
  val startDateTime: ZonedDateTime?,
  val notes: String?,
  val mainAddress: Location?,
)

data class OfficerInfo(
  val forename: String?,
  val surname: String?,
  val middlename: String?,
)
data class Location(
  val buildingName: String?,
  val addressNumber: String?,
  val streetName: String?,
  val district: String?,
  val town: String?,
  val county: String?,
  val postcode: String?,
  val noFixedAbode: Boolean,
)
