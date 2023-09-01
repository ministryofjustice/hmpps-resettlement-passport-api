package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.casenotesapi

import java.time.LocalDateTime

data class CaseNote(
  val caseNoteId: String,
  val offenderIdentifier: String,
  val type: String,
  val typeDescription: String,
  val subType: String,
  val subTypeDescription: String,
  val source: String,
  val creationDateTime: LocalDateTime,
  val occurrenceDateTime: LocalDateTime,
  val authorName: String,
  val authorUserId: String,
  val text: String,
  val eventId: Int,
  val sensitive: Boolean,
  val amendments: List<CaseNoteAmendment> = listOf(),
)

data class CaseNoteAmendment(
  val caseNoteAmendmentId: Long,
  val creationDateTime: LocalDateTime,
  val authorUserName: String,
  val authorName: String,
  val authorUserId: String,
  val additionalNoteText: String,
)
data class CaseNotes(
  val content: List<CaseNote>? = listOf(),
  val last: Boolean,
  val totalElements: Int?,
  val totalPages: Int?,
  val size: Int?,
  val number: Int?,
  val first: Boolean,
  val numberOfElements: Int?,
  val empty: Boolean,
)

enum class PathwayMap(val id: String) {
  ACCOM("ACCOMMODATION"),
  ATB("ATTITUDES_THINKING_AND_BEHAVIOUR"),
  CHDFAMCOM("CHDFAMCOMCHILDREN_FAMILIES_AND_COMMUNITY"),
  DRUG_ALCOHOL("DRUGS_AND_ALCOHOL"),
  ED_SKL_WRK("EDUCATION_SKILLS_AND_WORK"),
  FINANCE_ID("FINANCE_AND_ID"),
  HEALTH("HEALTH"),
  RESET("GENERAL"),

  // TODO: Below lines to be removed
  HIS("ACCOMMODATION"),
  OSE("ATTITUDES_THINKING_AND_BEHAVIOUR"),
  REP_IEP("GENERAL"),
}
