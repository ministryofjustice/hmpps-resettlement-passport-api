package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment

enum class TypeOfQuestion {
  RADIO,
  SHORT_TEXT,
  LONG_TEXT,
  ADDRESS,
  CHECKBOX,
}

enum class ValidationType {
  MANDATORY,
  OPTIONAL,
}
