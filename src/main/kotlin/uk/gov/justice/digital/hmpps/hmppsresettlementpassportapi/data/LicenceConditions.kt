package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

data class LicenceConditions(
  val licenceId: Long?,
  val status: String? = null,
  val startDate: String? = null,
  val expiryDate: String? = null,

  val standardLicenceConditions: List<Conditions>? = emptyList(),
  val otherLicenseConditions: List<Conditions>? = emptyList(),
  val changeStatus: Boolean? = false,
)

data class Conditions(
  val id: Long? = null,
  val image: Boolean,
  val text: String? = null,
  val sequence: Int? = null,
)
