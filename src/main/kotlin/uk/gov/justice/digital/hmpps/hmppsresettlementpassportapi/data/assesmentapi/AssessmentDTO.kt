package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.assesmentapi

import java.time.LocalDateTime

data class AssessmentDTO(
  val assessmentDate: LocalDateTime,
  var isBankAccountRequired: Boolean,
  var isIdRequired: Boolean,
  var idDocuments: Set<String>,
  val nomsId: String,
)
