package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

data class ProfileReset (
  val resetReason: ResetReason,
  val additionalDetails: String?,
)

enum class ResetReason {
  RECALL_TO_PRISON,
  RETURN_ON_NEW_SENTENCE,
  OTHER,
}