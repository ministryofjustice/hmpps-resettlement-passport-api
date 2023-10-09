package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.cvlapi

data class LicenceSummary(
  val licenceId: Long,
  val licenceType: String,
  val licenceStatus: String,
  val nomsId: String,
  val surname: String,
  val forename: String,
  val prisonCode: String,
  val prisonDescription: String,
  val probationAreaCode: String,
  val probationAreaDescription: String,
  val probationPduCode: String,
  val probationPduDescription: String,
  val probationLauCode: String,
  val probationLauDescription: String,
  val probationTeamCode: String,
  val conditionalReleaseDate: String,
  val actualReleaseDate: String,
  val crn: String,
  val dateOfBirth: String,
  val comUsername: String,
  val bookingId: String,
  val dateCreated: String,
  val approvedByName: String? = null,
  val approvedDate: String? = null,
)

data class LicenceRequest(
  val nomsId: List<String>,
)
