package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import java.time.LocalDate
import java.time.LocalDateTime

data class PoPUserResponse(
  val id: Long,
  val crn: String,
  val cprId: String,
  val email: String,
  val verified: Boolean,
  val creationDate: LocalDateTime? = null,
  val modifiedDate: LocalDateTime? = null,
  val nomsId: String,
  val oneLoginUrn: String? = null,
  val prisonId: String? = null,
  val releaseDate: LocalDate? = null,
)
