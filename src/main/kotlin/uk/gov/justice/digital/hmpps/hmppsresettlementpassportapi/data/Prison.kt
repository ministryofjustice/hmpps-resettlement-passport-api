package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

class Prison(id: String, name: String, active: Boolean) {
  val id: String = id
  val name: String = name.replace(" \\(.*\\)".toRegex(), "")
  val active: Boolean = active
}
