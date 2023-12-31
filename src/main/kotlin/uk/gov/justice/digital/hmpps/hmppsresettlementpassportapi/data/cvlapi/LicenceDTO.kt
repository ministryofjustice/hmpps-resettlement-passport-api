package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.cvlapi

data class Licence(
  val id: Long,
  val typeCode: String,
  val version: String? = null,
  val statusCode: String?,
  val nomsId: String? = null,
  val bookingNo: String? = null,
  val bookingId: Long? = null,
  val crn: String? = null,
  val pnc: String? = null,
  val cro: String? = null,
  val prisonCode: String? = null,
  val prisonDescription: String? = null,
  val prisonTelephone: String? = null,
  val forename: String? = null,
  val middleNames: String? = null,
  val surname: String? = null,
  val dateOfBirth: String? = null,
  val conditionalReleaseDate: String? = null,
  val actualReleaseDate: String? = null,
  val sentenceStartDate: String? = null,
  val sentenceEndDate: String? = null,
  val licenceStartDate: String? = null,
  val licenceExpiryDate: String? = null,
  val topupSupervisionStartDate: String? = null,
  val topupSupervisionExpiryDate: String? = null,
  val comUsername: String? = null,
  val comStaffId: Long? = null,
  val comEmail: String? = null,
  val probationAreaCode: String? = null,
  val probationAreaDescription: String? = null,
  val probationPduCode: String? = null,
  val probationPduDescription: String? = null,
  val probationLauCode: String? = null,
  val probationLauDescription: String? = null,
  val probationTeamCode: String? = null,
  val probationTeamDescription: String? = null,
  val appointmentPerson: String? = null,
  val appointmentTime: String? = null,
  val appointmentAddress: String? = null,
  val appointmentContact: String? = null,
  val spoDiscussion: String? = null,
  val vloDiscussion: String? = null,
  val approvedDate: String? = null,
  val approvedByUsername: String? = null,
  val approvedByName: String? = null,
  val supersededDate: String? = null,
  val dateCreated: String? = null,
  val createdByUsername: String? = null,
  val dateLastUpdated: String? = null,
  val updatedByUsername: String? = null,
  val standardLicenceConditions: List<StandardCondition>? = emptyList(),
  val standardPssConditions: List<StandardCondition>? = emptyList(),
  val additionalLicenceConditions: List<AdditionalCondition> = emptyList(),
  val additionalPssConditions: List<AdditionalCondition> = emptyList(),
  val bespokeConditions: List<BespokeCondition> = emptyList(),
  val isVariation: Boolean,
  val variationOf: Long? = null,
  val createdByFullName: String? = null,
)

data class BespokeCondition(
  val id: Long? = null,
  val sequence: Int? = null,
  val text: String? = null,
)

data class StandardCondition(
  val id: Long? = null,
  val code: String? = null,
  val sequence: Int? = null,
  val text: String? = null,
)

data class AdditionalCondition(
  val id: Long? = null,
  val code: String? = null,
  val version: String? = null,
  val category: String? = null,
  val sequence: Int? = null,
  val text: String? = null,
  val expandedText: String? = null,
  // val data: List<AdditionalConditionData> = emptyList(),
  val uploadSummary: List<AdditionalConditionUploadSummary> = emptyList(),
)

data class AdditionalConditionUploadSummary(
  val id: Long? = null,
  val filename: String? = null,
  val fileType: String? = null,
  val fileSize: Int = 0,
  val uploadedTime: String? = null,
  val description: String? = null,
  val thumbnailImage: String? = null,
  val uploadDetailId: Long? = null,
)
