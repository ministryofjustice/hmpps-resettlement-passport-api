package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.assesmentapi

import java.time.LocalDateTime

data class AssessmentDTO(
  val assessmentDate: LocalDateTime,
  val isBankAccountRequired: Boolean,
  val isIdRequired: Boolean,
  val idDocuments: Set<String>,
  val nomsId: String,
)
