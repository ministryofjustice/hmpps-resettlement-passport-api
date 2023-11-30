package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

data class PrisonerCountMetrics(
  val metrics: MutableMap<Prison, List<PrisonerCountMetric>> = mutableMapOf(),
)

data class PrisonerCountMetric(
  val status: StatusTag,
  val releaseDate: ReleaseDateTag,
  val value: Int,
)

enum class StatusTag(val label: String) {
  ALL("All"),
  NOT_STARTED("Not Started"),
  IN_PROGRESS("In Progress"),
  DONE("Done"),
}

enum class ReleaseDateTag(val label: String) {
  PAST("Past"),
  ALL_FUTURE("All Future"),
  TWELVE_WEEKS("12 Weeks"),
  TWENTY_FOUR_WEEKS("24 Weeks"),
}

data class PrisonerCountMetricsByReleaseDate(
  val twelveWeeks: PrisonerCounts,
  val twentyFourWeeks: PrisonerCounts,
  val allFuture: PrisonerCounts,
)

data class PrisonerCounts(
  val totalPopulation: Int?,
  val notStarted: Int?,
  val inProgress: Int?,
  val done: Int?,
)
