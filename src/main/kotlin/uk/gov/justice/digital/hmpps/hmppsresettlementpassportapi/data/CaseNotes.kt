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

enum class CaseNoteType(val displayName: String) {
  All("All"), // Includes case notes in RESET + GEN/RESET
  ACCOMMODATION(Pathway.ACCOMMODATION.displayName), // Each pathway includes any relevant BCST case notes
  ATTITUDES_THINKING_AND_BEHAVIOUR(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR.displayName),
  CHILDREN_FAMILIES_AND_COMMUNITY(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY.displayName),
  DRUGS_AND_ALCOHOL(Pathway.DRUGS_AND_ALCOHOL.displayName),
  EDUCATION_SKILLS_AND_WORK(Pathway.EDUCATION_SKILLS_AND_WORK.displayName),
  FINANCE_AND_ID(Pathway.FINANCE_AND_ID.displayName),
  HEALTH(Pathway.HEALTH.displayName),
  ;

  companion object {
    fun getByDisplayName(displayName: String?) = entries.firstOrNull { it.displayName == displayName }
  }
}

enum class CaseNotePathway {
  ACCOMMODATION,
  ATTITUDES_THINKING_AND_BEHAVIOUR,
  CHILDREN_FAMILIES_AND_COMMUNITY,
  DRUGS_AND_ALCOHOL,
  EDUCATION_SKILLS_AND_WORK,
  FINANCE_AND_ID,
  HEALTH,
  OTHER, // Any other case notes e.g. GEN/RESET or RESET/BCST not related to a pathway or anything else in RESET type
}

enum class DpsCaseNoteType {
  GEN,
  RESET,
}

enum class DpsCaseNoteSubType {
  ACCOM,
  ATB,
  CHDFAMCOM,
  DRUG_ALCOHOL,
  ED_SKL_WRK,
  FINANCE_ID,
  HEALTH,
  GEN,
  RESET,
  BCST,
  INR,
  PRR,
}

enum class DeliusCaseNoteType {
  IMMEDIATE_NEEDS_REPORT,
  PRE_RELEASE_REPORT,
}
