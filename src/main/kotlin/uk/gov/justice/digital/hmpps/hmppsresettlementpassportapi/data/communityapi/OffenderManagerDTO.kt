package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.communityapi

data class OffenderManagerDTO (
  val staffId: Long?,
  val isPrisonOffenderManager: Boolean?,
  val isUnallocated: Boolean?,
  val staff: Staff?,
)

data class Staff (
  val forenames: String?,
  val surname: String?,
)