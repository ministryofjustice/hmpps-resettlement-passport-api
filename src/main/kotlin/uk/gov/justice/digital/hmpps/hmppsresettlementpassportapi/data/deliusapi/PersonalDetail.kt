package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi

import java.time.LocalDate

data class PersonalDetail(
  val crn: String,
  val name: Name?,
  val dateOfBirth: LocalDate?,
  var contactDetails: ContactDetails?,
) {
  data class Name(val forename: String, val surname: String)

  data class ContactDetails(val telephone: String?, var mobile: String?, val email: String?)
}
