package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies.AccommodationResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies.FinanceAndIdResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies.GenericResettlementAssessmentQuestion

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.PROPERTY,
  property = "@class",
)
@JsonSubTypes(
  JsonSubTypes.Type(value = AccommodationResettlementAssessmentQuestion::class),
  JsonSubTypes.Type(value = FinanceAndIdResettlementAssessmentQuestion::class),
  JsonSubTypes.Type(value = GenericResettlementAssessmentQuestion::class),
)
interface IResettlementAssessmentQuestion {
  val id: String
  val title: String
  val subTitle: String?
  val type: TypeOfQuestion
  val options: List<Option>?
}

data class Option(
  val id: String,
  val displayText: String,
  val description: String? = null,
  val withAddress: Boolean = false,
)

enum class TypeOfQuestion {
  RADIO_WITH_ADDRESS,
  LIST_OF_PEOPLE,
  RADIO,
  SHORT_TEXT,
  LONG_TEXT,
  DATE,
  DROPDOWN,
  ADDRESS,
}

val yesNoOptions = listOf(
  Option(id = "YES", displayText = "Yes"),
  Option(id = "NO", displayText = "No"),
  Option(id = "NO_ANSWER", displayText = "No answer provided"),
)
