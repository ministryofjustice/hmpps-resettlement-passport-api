package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DeliusCaseNoteType
import java.time.OffsetDateTime

data class DeliusCaseNote(
  val type: DeliusCaseNoteType,
  val dateTime: OffsetDateTime,
  val notes: String,
  val author: DeliusAuthor,
  val description: String?,
)

data class DeliusAuthor(
  val prisonCode: String,
  val forename: String,
  val surname: String,
)
