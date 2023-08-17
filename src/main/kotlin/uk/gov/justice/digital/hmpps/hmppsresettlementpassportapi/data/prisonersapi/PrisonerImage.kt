package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonersapi

import java.time.LocalDate
import java.time.LocalDateTime

data class PrisonerImage(
  val imageId: String,
  val active: Boolean,
  val captureDate: LocalDate? = null,
  val captureDateTime: LocalDateTime? = null,
  val imageView: String,
  val imageOrientation: String? = null,
  val imageType: String? = null,
)
