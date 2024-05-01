package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import java.math.BigDecimal
import java.time.LocalDate

data class IdApplicationPost(
  val idType: String? = null,
  val applicationSubmittedDate: LocalDate? = null,
  val isPriorityApplication: Boolean? = null,
  var costOfApplication: BigDecimal? = null,
  var haveGro: Boolean? = null,
  var isUkNationalBornOverseas: Boolean? = null,
  var countryBornIn: String? = null,
  var caseNumber: String? = null,
  var courtDetails: String? = null,
  var driversLicenceType: String? = null,
  var driversLicenceApplicationMadeAt: String? = null,
)

data class IdApplicationPatch(
  var refundAmount: BigDecimal? = null,
  var isAddedToPersonalItems: Boolean? = null,
  var addedToPersonalItemsDate: LocalDate? = null,
  var status: String? = null,
  var dateIdReceived: LocalDate? = null,
)
