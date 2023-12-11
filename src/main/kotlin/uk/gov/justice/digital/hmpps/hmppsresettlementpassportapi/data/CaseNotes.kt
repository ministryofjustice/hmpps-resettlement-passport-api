package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import java.time.LocalDateTime

data class PathwayCaseNote(
  val caseNoteId: String,
  val pathway: CaseNotePathway,
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

enum class CaseNotePathway {
  All,
  ACCOMMODATION,
  ATTITUDES_THINKING_AND_BEHAVIOUR,
  CHILDREN_FAMILIES_AND_COMMUNITY,
  DRUGS_AND_ALCOHOL,
  EDUCATION_SKILLS_AND_WORK,
  FINANCE_AND_ID,
  HEALTH,
  GENERAL,
}

enum class CaseNoteType {
  GEN,
  RESET,
}

enum class CaseNoteSubType {
  ACCOM,
  ATB,
  CHDFAMCOM,
  DRUG_ALCOHOL,
  ED_SKL_WRK,
  FINANCE_ID,
  HEALTH,
  GEN,
  RESET,
}
