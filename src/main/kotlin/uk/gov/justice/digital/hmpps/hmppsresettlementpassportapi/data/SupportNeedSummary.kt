package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import java.time.LocalDate
import java.time.LocalDateTime

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

data class PrisonerSupportNeedWithNomsIdAndLatestUpdate(
  val prisonerSupportNeedId: Long,
  val nomsId: String,
  val pathway: Pathway,
  val prisonerSupportNeedCreatedDate: LocalDateTime,
  val latestUpdateId: Long?,
  val latestUpdateStatus: SupportNeedStatus?,
  val latestUpdateCreatedDate: LocalDateTime?,
)
