package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ProfileTagList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
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
  var assignedWorkerFirstname: String? = null,
  var assignedWorkerLastname: String? = null,
  val needs: List<SupportNeedSummary>,
  val lastReport: LastReport?,
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
  var mobile: String? = null,
  var telephone: String? = null,
  var email: String? = null,
  var prisonName: String? = null,
  var isHomeDetention: Boolean?,
  var isRecall: Boolean?,
)

data class PathwayStatus(
  val pathway: Pathway? = null,
  val status: Status? = null,
  val lastDateChange: LocalDate? = null,
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
  val resettlementReviewAvailable: Boolean,
  val immediateNeedsSubmitted: Boolean,
  val preReleaseSubmitted: Boolean,
  val isInWatchlist: Boolean,
  val profile: ProfileTagList,
)

data class LastReport(
  val type: ResettlementAssessmentType,
  val dateCompleted: LocalDate,
)
