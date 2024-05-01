package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data
import java.time.LocalDate

data class BankApplication(

  val nomsId: String? = null,
  val bankName: String? = null,
  val applicationSubmittedDate: LocalDate? = null,
  val bankResponseDate: LocalDate? = null,
  val resubmissionDate: LocalDate? = null,
  val status: String? = null,
  val isAddedToPersonalItems: Boolean? = null,
  val addedToPersonalItemsDate: LocalDate? = null,
)
