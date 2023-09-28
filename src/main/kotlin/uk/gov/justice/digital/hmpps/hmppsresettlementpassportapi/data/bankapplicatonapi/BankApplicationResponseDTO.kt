package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.bankapplicatonapi

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDateTime

data class BankApplicationResponseDTO(
  val id: Long,
  val prisoner: PrisonerEntity,
  val logs: List<BankApplicationLogDTO>,
  val applicationSubmittedDate: LocalDateTime,
  val currentStatus: String,
  val bankName: String,
  val bankResponseDate: LocalDateTime? = null,
  val isAddedToPersonalItems: Boolean? = null,
  val addedToPersonalItemsDate: LocalDateTime? = null,
)

data class BankApplicationLogDTO(
  val id: Long,
  val status: String,
  val changeDate: LocalDateTime,

)
