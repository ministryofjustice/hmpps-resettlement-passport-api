package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
import java.time.LocalDate

data class Prisoners(
  val prisonerNumber: String,
  val firstName: String,
  val middleNames: String? = null,
  val lastName: String,
  var releaseDate: LocalDate? = null,
  val releaseType: String? = null,
  val lastUpdatedDate: LocalDate? = null,
  val status: List<PathwayStatus>? = ArrayList(),
  val pathwayStatus: Status?,
  val homeDetentionCurfewEligibilityDate: LocalDate? = null,
  val paroleEligibilityDate: LocalDate? = null,
  val releaseEligibilityDate: LocalDate? = null,
  val releaseEligibilityType: String? = null,
  val releaseOnTemporaryLicenceDate: LocalDate? = null,
  val assessmentRequired: Boolean,
)

data class PrisonerPersonal(
  val prisonerNumber: String,
  val prisonId: String,
  val firstName: String,
  val middleNames: String? = null,
  val lastName: String,
  val releaseDate: LocalDate? = null,
  val releaseType: String? = null,
  var dateOfBirth: LocalDate? = null,
  var age: Int?,
  var location: String? = null,
  var facialImageId: String? = null,
)

data class PathwayStatus(
  val pathway: Pathway? = null,
  val status: Status? = null,
  val lastDateChange: LocalDate? = null,
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

data class Prisoner(
  val personalDetails: PrisonerPersonal?,
  var pathways: List<PathwayStatus>? = ArrayList(),
  val assessmentRequired: Boolean,
)
