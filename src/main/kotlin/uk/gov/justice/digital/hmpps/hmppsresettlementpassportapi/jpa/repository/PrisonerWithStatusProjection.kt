package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import java.time.LocalDateTime

interface PrisonerWithStatusProjection {
  val prisonerId: Long
  val nomsId: String
  val pathway: Pathway
  var pathwayStatus: Status
  var updatedDate: LocalDateTime?
}
