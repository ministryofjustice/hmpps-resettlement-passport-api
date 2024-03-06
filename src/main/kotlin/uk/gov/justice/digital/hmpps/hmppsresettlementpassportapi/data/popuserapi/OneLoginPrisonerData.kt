package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.popuserapi

import java.time.LocalDate

data class OneLoginPrisonerData(

  val urn: String? = null,
  val otp: String? = null,
  val email: String? = null,
  val prisonId: String? = null,
  val releaseDate: LocalDate? = null,
)
