package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import java.time.LocalDate

data class SupportNeedSummary(
  val pathway: Pathway,
  val reviewed: Boolean,
  val notStarted: Int,
  val inProgress: Int,
  val met: Int,
  val declined: Int,
  val lastUpdated: LocalDate?,
)

data class SupportNeedSummaryResponse(
  val needs: List<SupportNeedSummary>,
)
