package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

data class SupportNeeds(
  val supportNeeds: List<SupportNeed>,
)

data class SupportNeed(
  val id: Long,
  val title: String,
  val category: String?,
  val allowUserDesc: Boolean,
  val isUpdatable: Boolean,
  val isPreSelected: Boolean,
  val existingPrisonerSupportNeedId: Long?,
)
