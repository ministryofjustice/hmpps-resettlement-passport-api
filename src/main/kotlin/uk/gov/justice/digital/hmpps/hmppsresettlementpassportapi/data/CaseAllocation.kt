package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

data class CaseAllocation(

  val nomsIds: Array<String> = emptyArray(),
  val staffId: Int? = null,
  val staffFirstName: String? = null,
  val staffLastName: String? = null,
)

data class CaseAllocationPostResponse(
  val staffId: Int?,
  val staffFirstname: String,
  val staffLastname: String,
  val nomsId: String,
)
