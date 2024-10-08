package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.popuserapi

import java.time.LocalDate

data class OneLoginData(
  val urn: String,
  val otp: String,
  val email: String,
  val dob: LocalDate,
)
