package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

data class PathwayAndStatus(
  val pathway: Pathway,
  val status: Status,
)

data class PathwayStatusAndCaseNote(
  val pathway: Pathway,
  val status: Status,
  val caseNoteText: String,
)
