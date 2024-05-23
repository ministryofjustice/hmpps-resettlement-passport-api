package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import java.time.LocalDate

data class WorkReadinessStatusAndDetails(
        val workReadinessStatus: String?,
        val workReadinessStatusLastUpdated: LocalDate?,
)

