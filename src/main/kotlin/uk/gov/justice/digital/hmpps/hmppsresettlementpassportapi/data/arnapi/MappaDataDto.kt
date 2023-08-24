package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.arnapi

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class MappaDataDto (
  val level: Int?,
  val levelDescription: String?,
  val category: Int?,
  val categoryDescription: String?,
  val startDate: LocalDate?,
  val reviewDate: LocalDate?,
  val team: KeyValueDto?,
  val officer: StaffHumanDto?,
  val probationArea: KeyValueDto?,
  val notes: String?,
)

data class KeyValueDto (
  @JsonProperty("Code")
  val code: String?,
  @JsonProperty("Description")
  val description: String?,
)

data class StaffHumanDto (
  val code: String?,
  val forenames: String?,
  val surname: String?,
  val unallocated: Boolean?,
)
