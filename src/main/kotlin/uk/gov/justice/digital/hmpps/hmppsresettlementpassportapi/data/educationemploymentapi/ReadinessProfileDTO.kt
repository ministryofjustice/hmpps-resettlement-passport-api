package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.educationemploymentapi

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.EnumWithLabel
import java.time.LocalDateTime

data class ReadinessProfileDTO(
  val profileData: Profile?,
)

data class Profile(
  var status: ProfileStatus?,
  var statusChangeDate: LocalDateTime?,
)

enum class ProfileStatus : EnumWithLabel {
  NO_RIGHT_TO_WORK,
  SUPPORT_DECLINED,
  SUPPORT_NEEDED,
  READY_TO_WORK,
}
