package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

enum class Status {
  NOT_STARTED,
  IN_PROGRESS,
  SUPPORT_NOT_REQUIRED,
  SUPPORT_DECLINED,
  DONE,
  SUPPORT_REQUIRED,
  ;

  companion object {
    fun getCompletedStatuses() = listOf(SUPPORT_NOT_REQUIRED, SUPPORT_DECLINED, DONE)
  }
}
