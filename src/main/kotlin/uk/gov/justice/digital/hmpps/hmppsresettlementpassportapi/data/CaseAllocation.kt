package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

data class CaseAllocation(

  val nomsIds: Array<String> = emptyArray(),
  val staffId: Int? = null,
  val staffFirstName: String? = null,
  val staffLastName: String? = null,
)
