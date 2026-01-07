package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

enum class SupportNeedStatus(val displayName: String) {
  NOT_STARTED("Not started"),
  IN_PROGRESS("In progress"),
  MET("Met"),
  DECLINED("Declined"),
}
