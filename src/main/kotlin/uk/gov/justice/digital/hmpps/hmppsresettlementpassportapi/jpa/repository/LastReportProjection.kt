package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import java.time.LocalDateTime

interface LastReportProjection {
  val nomsId: String
  val assessmentType: ResettlementAssessmentType
  val createdDate: LocalDateTime
  val submissionDate: LocalDateTime?
}
