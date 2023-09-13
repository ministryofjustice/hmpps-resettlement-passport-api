package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.allocationapi

import com.fasterxml.jackson.annotation.JsonProperty

data class AllocationDTO(
  @JsonProperty("primary_pom")
  val primaryPom: AllocationStaffDTO,
  @JsonProperty("secondary_pom")
  val secondaryPom: AllocationStaffDTO,
)

data class AllocationStaffDTO(
  @JsonProperty("staff_id")
  val staffId: String?,
  val name: String?,
)
