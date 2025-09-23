package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.keyworkerapi

data class AllocationsDTO(
  val allocations: List<Allocation>?,
)

data class Allocation(
  val policy: PolicyDTO,
  val staffMember: StaffMemberDTO,
)

data class PolicyDTO(
  val code: String?,
  val description: String?,
)

data class StaffMemberDTO(
  val staffId: Long?,
  val firstName: String?,
  val lastName: String?,
)
