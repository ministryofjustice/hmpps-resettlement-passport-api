package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import java.time.LocalDate
import java.time.LocalDateTime

data class Accommodation(
  val referralDate: LocalDate?,
  val provider: String?,
  val team: String?,
  val officer: OfficerInfo?,
  val status: String?,
  val startDateTime: LocalDateTime?,
  val notes: String?,
  val mainAddress: String?,
  val message: String?,
)

data class OfficerInfo(
  val forename: String?,
  val surname: String?,
  val middlename: String?,
)
