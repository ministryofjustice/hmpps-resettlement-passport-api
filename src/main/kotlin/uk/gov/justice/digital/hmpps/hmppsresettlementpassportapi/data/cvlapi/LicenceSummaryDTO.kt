package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.cvlapi

data class LicenceSummary(
  val licenceId: Long,
  val licenceStatus: String,
  val dateCreated: String?,
)

data class LicenceRequest(
  val nomsId: List<String>,
)
