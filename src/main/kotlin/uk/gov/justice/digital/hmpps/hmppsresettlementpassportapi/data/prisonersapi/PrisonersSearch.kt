package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi

import java.time.LocalDate

data class PrisonersSearch(
  val prisonerNumber: String,
  val firstName: String,
  val middleNames: String? = null,
  val lastName: String,
  val releaseDate: LocalDate? = null,
  val confirmedReleaseDate: LocalDate? = null,
  val actualParoleDate: LocalDate? = null,
  val homeDetentionCurfewActualDate: LocalDate? = null,
  val conditionalReleaseDate: LocalDate? = null,
  val automaticReleaseDate: LocalDate? = null,
  val homeDetentionCurfewEligibilityDate: LocalDate? = null,
  val paroleEligibilityDate: LocalDate? = null,
  val nonDtoReleaseDateType: String? = null,
  val releaseOnTemporaryLicenceDate: LocalDate? = null,
  val dateOfBirth: LocalDate? = null,
  val youthOffender: Boolean? = null,
  val prisonId: String,
  val prisonName: String,
  val cellLocation: String? = null,
  var displayReleaseDate: LocalDate? = null,
  val recall: Boolean = false,
)

data class PrisonersSearchList(
  val content: List<PrisonersSearch>?,
  val pageSize: Int?,
  val page: Int?,
  val sortName: String?,
  val totalElements: Int?,
  val last: Boolean,
)
