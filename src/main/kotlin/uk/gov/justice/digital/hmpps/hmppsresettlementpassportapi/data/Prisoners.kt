package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import java.time.LocalDate

data class Prisoners(
  val prisonerNumber: String,
  val firstName: String,
  val middleNames: String? = null,
  val lastName: String,
  val releaseDate: LocalDate? = null,
  val releaseType: String? = null,
  val lastUpdatedDate: LocalDate? = null,
  var status: List<PathwayStatus>? = ArrayList(),
)

data class PathwayStatus(
  val pathway: String? = null,
  val status: String? = null,
)
data class PrisonerRequest(
  val earliestReleaseDate: String,
  val latestReleaseDate: String,
  val prisonIds: List<String>,
)
data class PrisonersList(
  val content: List<Prisoners>?,
  val pageSize: Int?,
  val page: Int?,
  val sortName: String?,
  val totalElements: Int?,
  val last: Boolean,
)
