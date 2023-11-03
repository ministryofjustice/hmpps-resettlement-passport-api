package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import java.time.LocalDateTime

data class CRSReferralsWithPathway(
  var pathway: Pathway,
  var referrals: List<CRSReferral>,
  var message: String,
)

data class CRSReferrals(
  var crsReferral: List<CRSReferral> = listOf(),
)

data class CRSReferral(
  var serviceCategories: List<String> = listOf(),
  val contractType: String,
  val referralCreatedAt: LocalDateTime?,
  val referralSentAt: LocalDateTime?,
  val interventionTitle: String?,
  val referringOfficer: String?,
  val responsibleOfficer: String?,
  val serviceProviderUser: String?,
  val serviceProviderLocation: String?,
  val serviceProviderName: String?,
  val draft: Boolean?,

)

data class CRSReferralResponse(
  var results: List<CRSReferralsWithPathway> = mutableListOf(),
)

data class ContractTypeAndServiceCategories(
  val contractType: String,
  val serviceCategories: List<String>,
)
