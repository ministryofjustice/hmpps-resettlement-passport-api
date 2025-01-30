package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import java.time.LocalDateTime

data class SupportNeedUpdates(
  val updates: List<SupportNeedUpdate>,
  val allPrisonerNeeds: List<PrisonerNeedIdAndTitle>,
  val size: Int,
  val page: Int,
  val sortName: String,
  val totalElements: Int,
  val last: Boolean,
)

data class SupportNeedUpdate(
  val id: Long,
  val title: String,
  val status: SupportNeedStatus,
  val isPrisonResponsible: Boolean,
  val isProbationResponsible: Boolean,
  val text: String?,
  val createdBy: String,
  val createdAt: LocalDateTime,
)

data class PrisonerNeedIdAndTitle(
  val id: Long,
  val title: String,
)
