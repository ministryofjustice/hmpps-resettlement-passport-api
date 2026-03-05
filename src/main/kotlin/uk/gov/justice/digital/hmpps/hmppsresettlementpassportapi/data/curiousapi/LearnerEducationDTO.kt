package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.curiousapi

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

@JvmRecord
data class LearnerEducationDTO(
  @param:JsonProperty("prn")
  val nomsId: String,
  @param:JsonProperty("establishmentId")
  val establishmentId: String?,
  @param:JsonProperty("establishmentName")
  val establishmentName: String?,
  @param:JsonProperty("courseName")
  val courseName: String?,
  @param:JsonProperty("courseCode")
  val courseCode: String?,
  @param:JsonProperty("isAccredited")
  val isAccredited: Boolean?,
  @param:JsonProperty("aimSequenceNumber")
  val aimSequenceNumber: Int?,
  @param:JsonProperty("learningStartDate")
  val learningStartDate: LocalDate?,
  @param:JsonProperty("learningPlannedEndDate")
  val learningPlannedEndDate: LocalDate?,
  @param:JsonProperty("learningActualEndDate")
  val learningActualEndDate: LocalDate?,
  @param:JsonProperty("learnersAimType")
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

@JvmRecord
data class LearnersEducationList(
  val content: MutableList<LearnerEducationDTO>?,
  val empty: Boolean,
  val first: Boolean,
  val last: Boolean,
  val number: Int = 0,
  val numberOfElements: Int = 0,
  val pageable: Pageable?,
  val size: Int = 0,
  val sort: Sort?,
  val totalElements: Int = 0,
  val totalPages: Int = 0,
)

@JvmRecord
data class Pageable(
  val offset: Int?,
  val pageNumber: Int?,
  val pageSize: Int?,
  val paged: Boolean,
  val sort: Sort?,
  val unpaged: Boolean,
)

@JvmRecord
data class Sort(
  val empty: Boolean,
  val sorted: Boolean,
  val unsorted: Boolean,
)
