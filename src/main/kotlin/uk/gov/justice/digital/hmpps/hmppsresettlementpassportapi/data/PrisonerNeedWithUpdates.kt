package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

data class PrisonerNeedWithUpdates(
  val title: String,
  val isPrisonResponsible: Boolean?,
  val isProbationResponsible: Boolean?,
  val status: SupportNeedStatus?,
  val previousUpdates: List<SupportNeedUpdate>,
)
