package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.cvlapi


data class LicenceConditions(

  var licenceId : Long?,
  var status: String?,

  var standardLicenceConditions: List<Conditions>? = emptyList(),
  var otherLicenseConditions: List<Conditions>? = emptyList(),

  )

data class Conditions(
  var id: Long? = null,
  var image: Boolean,
  var text: String? = null,
)
