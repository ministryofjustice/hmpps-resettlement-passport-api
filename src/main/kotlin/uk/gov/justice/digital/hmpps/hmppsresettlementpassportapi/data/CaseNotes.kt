package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import java.time.LocalDateTime

data class PathwayCaseNote(
  val caseNoteId: String,
  val pathway: String,
  val creationDateTime: LocalDateTime,
  val occurenceDateTime: LocalDateTime,
  val createdBy: String,
  val text: String,
)

data class CaseNotesList(
  val content: List<PathwayCaseNote>?,
  val pageSize: Int?,
  val page: Int?,
  val sortName: String?,
  val totalElements: Int?,
  val last: Boolean,
)

data class CaseNotesMeta(
  val createdBy: String,
  val userId: String,
)

data class CaseNotesRequest(
  val pathway: Pathway,
  val text: String,
)
