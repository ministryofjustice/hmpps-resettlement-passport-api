package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.popuserapi

import java.time.LocalDate

data class OneLoginData(
  val urn: String? = null,
  val otp: String? = null,
  val email: String? = null,
  val dob: LocalDate? = null,
)
