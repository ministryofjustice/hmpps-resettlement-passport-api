package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.interventionsapi

data class Referral(
  val serviceCategories: List<String> = listOf(),
  val contractType: String?,
  val referralCreatedAt: String?,
  val referralSentAt: String?,
  val interventionTitle: String?,
  val referringOfficer: String?,
  val responsibleOfficer: String?,
  val serviceProviderUser: String?,
  val serviceProviderLocation: String?,
  val serviceProviderName: String?,
  val isDraft: Boolean,

)
