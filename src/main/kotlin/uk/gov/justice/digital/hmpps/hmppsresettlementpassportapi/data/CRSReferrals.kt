package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

data class CRSReferralsWithPathway(
  var pathway: String,
  var referrals: List<CRSReferral>,
  var message: String,
)

data class CRSReferrals(
  var crsReferral: List<CRSReferral> = listOf(),
)

data class CRSReferral(
  var serviceCategories: List<String> = listOf(),
  val contractType: String?,
  val referralCreatedAt: String?,
  val referralSentAt: String?,
  val interventionTitle: String?,
  val referringOfficer: String?,
  val responsibleOfficer: String?,
  val serviceProviderUser: String?,
  val serviceProviderLocation: String?,
  val serviceProviderName: String?,
  val draft: Boolean?,

)

data class CRSReferralResponse(
  var results: List<CRSReferralsWithPathway> = mutableListOf<CRSReferralsWithPathway>(),
)
