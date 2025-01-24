package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import java.time.LocalDate
import java.time.LocalDateTime

data class SupportNeedSummary(
  val pathway: Pathway,
  val reviewed: Boolean,
  var notStarted: Int,
  var inProgress: Int,
  var met: Int,
  var declined: Int,
  var lastUpdated: LocalDate?,
)

data class SupportNeedSummaryResponse(
  val needs: List<SupportNeedSummary>,
)

data class PrisonerSupportNeedWithNomsIdAndLatestUpdate(
  val prisonerSupportNeedId: Long,
  val nomsId: String,
  val pathway: Pathway,
  val prisonerSupportNeedCreatedDate: LocalDateTime,
  val latestUpdateId: Long?,
  val latestUpdateStatus: SupportNeedStatus?,
  val latestUpdateCreatedDate: LocalDateTime?,
)
