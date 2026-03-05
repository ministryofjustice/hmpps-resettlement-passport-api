package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi

import java.time.LocalDate

@JvmRecord
data class PersonalDetail(
  val crn: String,
  val name: Name?,
  val dateOfBirth: LocalDate?,
  val contactDetails: ContactDetails?,
) {
  @JvmRecord
  data class Name(val forename: String, val surname: String)

  @JvmRecord
  data class ContactDetails(val telephone: String?, val mobile: String?, val email: String?)
}
