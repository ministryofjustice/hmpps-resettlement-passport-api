package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.interventionsapi

import java.time.OffsetDateTime

data class ReferralDTO(
  val serviceCategories: List<String> = listOf(),
  val contractType: String,
  val referralCreatedAt: OffsetDateTime?,
  val referralSentAt: OffsetDateTime?,
  val interventionTitle: String?,
  val referringOfficer: String?,
  val responsibleOfficer: String?,
  val serviceProviderUser: String?,
  val serviceProviderLocation: String?,
  val serviceProviderName: String?,
  val isDraft: Boolean,

)
