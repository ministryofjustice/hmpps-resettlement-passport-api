package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment

import java.time.LocalDate

data class OneLoginUserData(

  val urn: String? = null,
  val otp: String? = null,
  val email: String? = null,
  val prisonId: String? = null,
  val releaseDate: LocalDate? = null,
)
