package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

data class StaffContacts(
  val primaryPom: Contact?,
  val secondaryPom: Contact?,
  val com: Contact?,
  val keyWorker: Contact?,
  val resettlementWorker: Contact?,
)

data class Contact(
  val name: String,
)
