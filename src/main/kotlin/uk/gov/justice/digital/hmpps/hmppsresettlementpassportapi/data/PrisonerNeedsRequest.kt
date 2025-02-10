package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

data class PrisonerNeedsRequest(
  val needs: List<PrisonerNeedRequest>,
)

data class PrisonerNeedRequest(
  val needId: Long,
  val prisonerSupportNeedId: Long?,
  val otherDesc: String?,
  val text: String?,
  val status: SupportNeedStatus?,
  val isPrisonResponsible: Boolean?,
  val isProbationResponsible: Boolean?,
)

data class SupportNeedsUpdateRequest(
  val text: String?,
  val status: SupportNeedStatus,
  val isPrisonResponsible: Boolean,
  val isProbationResponsible: Boolean,
)
