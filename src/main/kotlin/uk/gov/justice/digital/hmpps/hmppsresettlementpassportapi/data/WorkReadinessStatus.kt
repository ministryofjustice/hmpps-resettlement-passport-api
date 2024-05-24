package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import java.time.LocalDate

data class WorkReadinessStatus(
  val workReadinessStatus: String?,
  val workReadinessStatusLastUpdated: LocalDate?,
)
