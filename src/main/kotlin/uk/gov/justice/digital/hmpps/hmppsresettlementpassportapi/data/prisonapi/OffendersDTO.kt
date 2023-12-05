package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonapi

import java.time.LocalDate
import java.time.LocalDateTime

data class Offenders(
  var prisonerNumber: String,
  var pncNumber: String? = null,
  var pncNumberCanonicalShort: String? = null,
  var pncNumberCanonicalLong: String? = null,
  var croNumber: String? = null,
  var bookingId: String? = null,
  var bookNumber: String? = null,
  var firstName: String? = null,
  var middleNames: String? = null,
  var lastName: String? = null,
  var dateOfBirth: LocalDate? = null,
  var gender: String? = null,
  var ethnicity: String? = null,
  var youthOffender: Boolean,
  var maritalStatus: String? = null,
  var religion: String? = null,
  var nationality: String? = null,
  var status: String? = null,
  var lastMovementTypeCode: String? = null,
  var lastMovementReasonCode: String? = null,
  var inOutStatus: String? = null,
  var prisonId: String? = null,
  var prisonName: String? = null,
  var cellLocation: String? = null,
  var aliases: List<PrisonerAlias>,
  var alerts: List<PrisonerAlert>,
  var csra: String? = null,
  var category: String? = null,
  var legalStatus: String? = null,
  var imprisonmentStatus: String? = null,
  var imprisonmentStatusDescription: String? = null,
  var mostSeriousOffence: String? = null,
  var recall: Boolean,
  var indeterminateSentence: Boolean,
  var sentenceStartDate: LocalDate? = null,
  var releaseDate: LocalDate? = null,
  var confirmedReleaseDate: LocalDate? = null,
  var sentenceExpiryDate: LocalDate? = null,
  var licenceExpiryDate: LocalDate? = null,
  var homeDetentionCurfewEligibilityDate: LocalDate? = null,
  var homeDetentionCurfewActualDate: LocalDate? = null,
  var homeDetentionCurfewEndDate: LocalDate? = null,
  var topupSupervisionStartDate: LocalDate? = null,
  var topupSupervisionExpiryDate: LocalDate? = null,
  var additionalDaysAwarded: Int? = null,
  var nonDtoReleaseDate: LocalDate? = null,
  var nonDtoReleaseDateType: String? = null,
  var receptionDate: LocalDate? = null,
  var paroleEligibilityDate: LocalDate? = null,
  var automaticReleaseDate: LocalDate? = null,
  var postRecallReleaseDate: LocalDate? = null,
  var conditionalReleaseDate: LocalDate? = null,
  var actualParoleDate: LocalDate? = null,
  var tariffDate: LocalDate? = null,
  var releaseOnTemporaryLicenceDate: LocalDate? = null,
  var locationDescription: String? = null,
  var restrictedPatient: Boolean = false,
  var supportingPrisonId: String? = null,
  var dischargedHospitalId: String? = null,
  var dischargedHospitalDescription: String? = null,
  var dischargeDate: LocalDate? = null,
  var dischargeDetails: String? = null,
  var currentIncentive: CurrentIncentive,
  var heightCentimetres: Int? = 0,
  var weightKilograms: Int? = 0,
  var hairColour: String? = null,
  var rightEyeColour: String? = null,
  var leftEyeColour: String? = null,
  var facialHair: String? = null,
  var shapeOfFace: String? = null,
  var build: String? = null,
  var shoeSize: Int? = 0,
  var tattoos: List<BodyPartDetail>? = null,
  var scars: List<BodyPartDetail>? = null,
  var marks: List<BodyPartDetail>? = null,
  var otherMarks: List<BodyPartDetail>? = null,
)

data class PrisonerAlias(
  val firstName: String? = null,
  val middleNames: String? = null,
  val lastName: String? = null,
  val dateOfBirth: LocalDate? = null,
  val gender: String? = null,
  val ethnicity: String? = null,
)

data class PrisonerAlert(
  val alertType: String? = null,
  val alertCode: String? = null,
  val active: Boolean,
  val expired: Boolean,
)

data class CurrentIncentive(
  val level: IncentiveLevel,
  val dateTime: LocalDateTime? = null,
  val nextReviewDate: LocalDate? = null,
)

data class IncentiveLevel(
  val code: String?,
  val description: String? = null,
)

data class BodyPartDetail(
  var bodyPart: String? = null,
  var comment: String? = null,
)

data class OffendersList(
  val content: List<Offenders>,
  val totalElements: Int,
  val last: Boolean,
  var size: Int,
  var number: Int,
  var totalPages: Int,
)
