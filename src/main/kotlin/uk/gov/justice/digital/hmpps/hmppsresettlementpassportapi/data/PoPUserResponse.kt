package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import java.time.LocalDateTime

data class PoPUserResponse(
  val id: Long,
  val crn: String,
  val cprId: String,
  val verified: Boolean,
  val creationDate: LocalDateTime? = null,
  val modifiedDate: LocalDateTime? = null,
  val nomsId: String,
  val oneLoginUrn: String? = null,
)

data class PoPUserOTP(
  val id: Long?,
  val creationDate: LocalDateTime? = null,
  val expiryDate: LocalDateTime? = null,
  val otp: String,
)
