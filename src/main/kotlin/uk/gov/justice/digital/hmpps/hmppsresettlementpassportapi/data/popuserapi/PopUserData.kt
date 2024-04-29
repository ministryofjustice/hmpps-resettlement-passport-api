package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.popuserapi

import java.time.LocalDate
import java.time.LocalDateTime

data class PopUserData(
  val id: Long,
  val crn: String? = null,
  val cprId: String? = null,
  val verified: Boolean? = null,
  val nomsId: String? = null,
  val oneLoginUrn: String? = null,
  val creationDate: LocalDateTime? = null,
  val modifiedDate: LocalDateTime? = null,
)
