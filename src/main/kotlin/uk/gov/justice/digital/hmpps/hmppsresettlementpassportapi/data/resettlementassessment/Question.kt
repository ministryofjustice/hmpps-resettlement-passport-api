package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment

interface IResettlementAssessmentQuestion {
  val id: String
  val title: String
  val subTitle: String?
  val type: TypeOfQuestion
  val options: List<Option>?
  val validationType: ValidationType
}

data class Option(
  val id: String,
  val displayText: String,
  val description: String? = null,
  val exclusive: Boolean = false,
)

enum class TypeOfQuestion {
  RADIO,
  SHORT_TEXT,
  LONG_TEXT,
  ADDRESS,
  CHECKBOX,
}

val yesNoOptions = listOf(
  Option(id = "YES", displayText = "Yes"),
  Option(id = "NO", displayText = "No"),
  Option(id = "NO_ANSWER", displayText = "No answer provided"),
)

enum class ValidationType {
  MANDATORY,
  OPTIONAL,
}
