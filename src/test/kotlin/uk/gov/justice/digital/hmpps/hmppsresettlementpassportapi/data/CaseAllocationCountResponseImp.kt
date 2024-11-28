package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

data class CaseAllocationCountResponseImp(
  override val staffId: Int,
  override val firstName: String?,
  override val lastName: String?,
  override val casesAssigned: Int?,
) : CaseAllocationCountResponse
