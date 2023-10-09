package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data
import java.time.LocalDateTime

data class BankApplication(

  val nomsId: String? = null,
  val bankName: String? = null,
  val applicationSubmittedDate: LocalDateTime? = null,
  val bankResponseDate: LocalDateTime? = null,
  val resubmissionDate: LocalDateTime? = null,
  val status: String? = null,
  val isAddedToPersonalItems: Boolean? = null,
  val addedToPersonalItemsDate: LocalDateTime? = null,
)
