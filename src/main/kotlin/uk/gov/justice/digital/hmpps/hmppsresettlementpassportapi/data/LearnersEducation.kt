package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import java.time.LocalDate

data class LearnersCourse(
  val nomsId: String,
  val establishmentId: String?,
  val establishmentName: String?,
  val courseName: String?,
  val courseCode: String?,
  val isAccredited: Boolean?,
  val aimSequenceNumber: Int?,
  val learningStartDate: LocalDate?,
  val learningPlannedEndDate: LocalDate?,
  val learningActualEndDate: LocalDate?,
  val learnersAimType: String?,
  val completionStatus: String?,
)

data class LearnersCourseList(
  val content: List<LearnersCourse>?,
  val pageSize: Int?,
  val page: Int?,
  val totalElements: Int?,
  val last: Boolean,
)
