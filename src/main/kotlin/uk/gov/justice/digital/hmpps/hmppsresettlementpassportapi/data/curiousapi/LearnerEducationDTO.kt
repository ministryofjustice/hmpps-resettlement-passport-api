package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.curiousapi

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class LearnerEducationDTO(
  @JsonProperty("prn")
  val nomsId: String,
  @JsonProperty("establishmentId")
  val establishmentId: String?,
  @JsonProperty("establishmentName")
  val establishmentName: String?,
  @JsonProperty("courseName")
  val courseName: String?,
  @JsonProperty("courseCode")
  val courseCode: String?,
  @JsonProperty("isAccredited")
  val isAccredited: Boolean?,
  @JsonProperty("aimSequenceNumber")
  val aimSequenceNumber: Int?,
  @JsonProperty("learningStartDate")
  val learningStartDate: LocalDate?,
  @JsonProperty("learningPlannedEndDate")
  val learningPlannedEndDate: LocalDate?,
  @JsonProperty("learningActualEndDate")
  val learningActualEndDate: LocalDate?,
  @JsonProperty("learnersAimType")
  val learnersAimType: String?,
  val miNotionalNVQLevelV2: String?,
  val sectorSubjectAreaTier1: String?,
  val sectorSubjectAreaTier2: String?,
  val occupationalIndicator: Boolean?,
  val accessHEIndicator: Boolean?,
  val keySkillsIndicator: Boolean?,
  val functionalSkillsIndicator: Boolean?,
  val gceIndicator: Boolean?,
  val gcsIndicator: Boolean?,
  val asLevelIndicator: Boolean?,
  val a2LevelIndicator: Boolean?,
  val qcfIndicator: Boolean?,
  val qcfDiplomaIndicator: Boolean?,
  val qcfCertificateIndicator: Boolean?,
  val lrsGLH: Int?,
  val attendedGLH: Int?,
  val actualGLH: Int,
  val outcome: String?,
  val outcomeGrade: String?,
  val employmentOutcome: String?,
  val withdrawalReasons: String?,
  val prisonWithdrawalReason: String?,
  val completionStatus: String?,
  val withdrawalReasonAgreed: Boolean,
  val fundingModel: String?,
  val fundingAdjustmentPriorLearning: Int?,
  val subcontractedPartnershipUKPRN: Int?,
  val deliveryLocationPostCode: String?,
  val unitType: String?,
  val fundingType: String?,
  val deliveryMethodType: String?,
  val alevelIndicator: Boolean?,

)

data class LearnersEducationList(
  val content: MutableList<LearnerEducationDTO>?,
  val empty: Boolean,
  val first: Boolean,
  val last: Boolean,
  val number: Int = 0,
  val numberOfElements: Int = 0,
  val pageable: Pageable?,
  val size: Int = 0,
  val sort: List<Any>,
  var totalElements: Int = 0,
  val totalPages: Int = 0,
)

data class Pageable(
  val offset: Int?,
  val pageNumber: Int?,
  val pageSize: Int?,
  val paged: Boolean,
  val sort: List<Any>,
  val unpaged: Boolean,
)

data class Sort(
  val empty: Boolean,
  val sorted: Boolean,
  val unsorted: Boolean,
)
