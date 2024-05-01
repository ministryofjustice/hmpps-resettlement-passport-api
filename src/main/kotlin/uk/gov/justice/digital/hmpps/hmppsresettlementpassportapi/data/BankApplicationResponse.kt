package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import java.time.LocalDate

data class BankApplicationResponse(
  val id: Long,
  val prisoner: PrisonerEntity,
  val logs: List<BankApplicationLog>,
  val applicationSubmittedDate: LocalDate,
  val currentStatus: String,
  val bankName: String,
  val bankResponseDate: LocalDate? = null,
  val isAddedToPersonalItems: Boolean? = null,
  val addedToPersonalItemsDate: LocalDate? = null,
)

data class BankApplicationLog(
  val id: Long,
  val status: String,
  val changeDate: LocalDate,

)
