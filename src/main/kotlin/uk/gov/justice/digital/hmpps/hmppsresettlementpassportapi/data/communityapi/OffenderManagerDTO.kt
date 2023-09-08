package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.communityapi

data class OffenderManagerDTO(
  val staffId: Long?,
  val isPrisonOffenderManager: Boolean?,
  val isUnallocated: Boolean?,
  val staff: StaffDTO?,
)

data class StaffDTO(
  val forenames: String?,
  val surname: String?,
)
