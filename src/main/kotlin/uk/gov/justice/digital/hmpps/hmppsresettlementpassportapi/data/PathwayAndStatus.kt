package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import jakarta.validation.Valid
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status

data class PathwayAndStatus (
  @field:Valid
  val pathway: Pathway,
  @field:Valid
  val status: Status,
)
