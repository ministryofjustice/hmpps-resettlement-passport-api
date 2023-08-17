package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi

import java.time.LocalDate

data class PrisonersSearch(
  val prisonerNumber: String,
  val firstName: String,
  val middleNames: String? = null,
  val lastName: String,
  val releaseDate: LocalDate? = null,
  val nonDtoReleaseDateType: String? = null,
  val dateOfBirth: LocalDate? = null,
  val age: Int? = 0,
  val prisonId: String,
  val prisonName: String,
)

data class PrisonerRequest(
  val earliestReleaseDate: String,
  val latestReleaseDate: String,
  val prisonIds: List<String>,
)
data class PrisonersSearchList(
  val content: List<PrisonersSearch>?,
  val pageSize: Int?,
  val page: Int?,
  val sortName: String?,
  val totalElements: Int?,
  val last: Boolean,
)


