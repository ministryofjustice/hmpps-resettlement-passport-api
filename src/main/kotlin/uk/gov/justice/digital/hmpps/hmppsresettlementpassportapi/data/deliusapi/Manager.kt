package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.deliusapi

data class Manager(
  val name: Name?,
  val unallocated: Boolean,
) {
  data class Name(val forename: String, val surname: String)
}
