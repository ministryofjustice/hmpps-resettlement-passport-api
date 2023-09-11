package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.communityapi

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.LocalDateTime

data class OffenderDetailSummaryDTO(
  val offenderId: Long,
  val title: String?,
  val firstName: String?,
  val middleNames: List<String>?,
  val surname: String?,
  val previousSurname: String?,
  val preferredName: String?,
  val dateOfBirth: LocalDate?,
  val gender: String?,
  val otherIds: IDs?,
  val contactDetails: ContactDetailsSummary?,
  val offenderProfile: OffenderProfile?,
  val softDeleted: Boolean?,
  val currentDisposal: String?,
  val partitionArea: String?,
  val currentRestriction: Boolean?,
  val currentExclusion: Boolean?,
  val activeProbationManagedSentence: Boolean?,
)

data class IDs(
  val crn: String,
  val pncNumber: String?,
  val croNumber: String?,
  val niNumber: String?,
  val nomsNumber: String?,
  val immigrationNumber: String?,
  val mostRecentPrisonerNumber: String?,
)

data class ContactDetailsSummary(
  val phoneNumbers: List<PhoneNumber>?,
  val emailAddresses: List<String>?,
  val allowSms: Boolean?,
)

data class PhoneNumber(
  val type: PhoneTypes?,
  val number: String?,
)

enum class PhoneTypes {
  TELEPHONE,
  MOBILE,
}

data class OffenderProfile(
  val ethnicity: String?,
  val nationality: String?,
  val secondaryNationality: String?,
  val notes: String?,
  val immigrationStatus: String?,
  val offenderLanguages: OffenderLanguages?,
  val religion: String?,
  val sexualOrientation: String?,
  val offenderDetails: String?,
  val remandStatus: String?,
  val previousConviction: PreviousConviction?,
  val riskColour: String?,
  val disabilities: List<Disability>?,
  val provisions: List<Provision>?,
  @JsonProperty("Gender identity")
  val genderIdentity: String?,
  @JsonProperty("Self described gender identity")
  val selfDescribedGenderIdentity: String?,
)

data class OffenderLanguages(
  val primaryLanguage: String?,
  val otherLanguages: List<String>?,
  val languageConcerns: String?,
  val requiresInterpreter: Boolean?,
)

data class PreviousConviction(
  val convictionDate: LocalDate?,
  val detail: Map<String, String>?,
)

data class Disability(
  val disabilityId: Long?,
  val disabilityType: KeyValue?,
  val disabilityCondition: KeyValue?,
  val startDate: LocalDate?,
  val endDate: LocalDate?,
  val notes: String?,
  val provisions: List<Provision>?,
  @JsonProperty("Date time when disability was last updated")
  val lastUpdatedDateTime: LocalDateTime?,
  val isActive: Boolean?,
)

data class KeyValue(
  @JsonProperty("Code")
  val code: String?,
  @JsonProperty("Description")
  val description: String?,
)

data class Provision(
  val provisionId: Long?,
  val notes: String?,
  val startDate: LocalDate?,
  val finishDate: LocalDate?,
  val provisionType: KeyValue?,
  val category: KeyValue?,
)
