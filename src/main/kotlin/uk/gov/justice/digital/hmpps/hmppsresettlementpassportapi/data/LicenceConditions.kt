package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data
data class LicenceConditions(
  var licenceId: Long?,
  var status: String?,
  var startDate: String?,
  var expiryDate: String?,

  var standardLicenceConditions: List<Conditions>? = emptyList(),
  var otherLicenseConditions: List<Conditions>? = emptyList(),
  var changeStatus: Boolean? = false,
)

data class Conditions(
  var id: Long? = null,
  var image: Boolean,
  var text: String? = null,
  var sequence: Int? = null,
)
