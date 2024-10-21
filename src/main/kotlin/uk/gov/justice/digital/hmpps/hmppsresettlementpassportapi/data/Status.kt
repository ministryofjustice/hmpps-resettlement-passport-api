package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

enum class Status(val displayText: String) {
  NOT_STARTED("Not started"),
  IN_PROGRESS("In progress"),
  SUPPORT_NOT_REQUIRED("Support not required"),
  SUPPORT_DECLINED("Support declined"),
  DONE("Done"),
  SUPPORT_REQUIRED("Support required"),
}
