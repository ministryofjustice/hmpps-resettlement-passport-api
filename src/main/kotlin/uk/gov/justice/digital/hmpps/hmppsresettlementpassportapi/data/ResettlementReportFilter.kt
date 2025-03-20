package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType

enum class ResettlementReportFilter(val displayText: String, val assessmentType: ResettlementAssessmentType? = null) {
  BCST2("Immediate needs", ResettlementAssessmentType.BCST2),
  RESETTLEMENT_PLAN("Pre-release", ResettlementAssessmentType.RESETTLEMENT_PLAN),
  NONE("No reports completed"),
}
