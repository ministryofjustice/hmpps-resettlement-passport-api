package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import com.fasterxml.jackson.annotation.JsonUnwrapped

data class LicenceConditions(
  val licenceId: Long?,
  val status: String? = null,
  val startDate: String? = null,
  val expiryDate: String? = null,

  val standardLicenceConditions: List<Conditions>? = emptyList(),
  val otherLicenseConditions: List<Conditions>? = emptyList(),
)

data class Conditions(
  val id: Long? = null,
  val image: Boolean,
  val text: String? = null,
  val sequence: Int? = null,
)

data class LicenceConditionsWithMetaData(
  @field:JsonUnwrapped
  val licenceConditions: LicenceConditions,
  @field:JsonUnwrapped
  val licenceConditionsMetadata: LicenceConditionsMetadata? = null,
)

data class LicenceConditionsMetadata(
  val changeStatus: Boolean?,
  val version: Int,
)
