package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.allocationapi

import com.fasterxml.jackson.annotation.JsonProperty

data class AllocationDTO(
  @param:JsonProperty("primary_pom")
  val primaryPom: AllocationStaffDTO,
  @param:JsonProperty("secondary_pom")
  val secondaryPom: AllocationStaffDTO,
)

data class AllocationStaffDTO(
  @param:JsonProperty("staff_id")
  val staffId: String?,
  val name: String?,
)
