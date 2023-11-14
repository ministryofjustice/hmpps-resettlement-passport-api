package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status

data class PathwayAndStatus(
  val pathway: Pathway,
  val status: Status,
)

data class PathwayStatusAndCaseNote(
  val pathway: Pathway,
  val status: Status,
  val caseNoteText: String,
)
