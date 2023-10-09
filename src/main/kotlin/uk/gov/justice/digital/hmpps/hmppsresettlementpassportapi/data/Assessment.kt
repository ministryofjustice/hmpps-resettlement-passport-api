package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import java.time.LocalDateTime

data class Assessment(
  val assessmentDate: LocalDateTime,
  val isBankAccountRequired: Boolean,
  val isIdRequired: Boolean,
  val idDocuments: Set<String>,
  val nomsId: String,
)
