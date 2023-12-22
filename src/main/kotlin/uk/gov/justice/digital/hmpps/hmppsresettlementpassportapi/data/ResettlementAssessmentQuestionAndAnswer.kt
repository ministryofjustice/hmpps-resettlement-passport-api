package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

data class ResettlementAssessmentQuestionAndAnswer<T> (
  val question: ResettlementAssessmentQuestions,
  val answer: T,
)

interface ResettlementAssessmentQuestions {
  val title: String
  val subTitle: String?
  val type: TypeOfQuestion
  val options: List<Option>?
}

data class Option(
  val id: String,
  val displayText: String,
)

enum class AccommodationResettlementAssessmentQuestions(
  override val title: String,
  override val subTitle: String? = null,
  override val type: TypeOfQuestion,
  override val options: List<Option>? = null,
) : ResettlementAssessmentQuestions {
  WHERE_WILL_THEY_LIVE(title = "", type = TypeOfQuestion.RADIO_WITH_ADDRESS, options = listOf(
    Option(id = "PREVIOUS_ADDRESS", displayText = "Returning to a previous address"),
    Option(id = "NEW_ADDRESS", displayText = "Moving to new address"),
    Option(id = "NO_PLACE_TO_LIVE", displayText = "No place to live"),
    )),
  WHO_WILL_THEY_LIVE_WITH(title = "", type = TypeOfQuestion.LIST_OF_PEOPLE),
  WHAT_IS_THE_ADDRESS(title = "", type = TypeOfQuestion.ADDRESS),
  ACCOM_CRS(title = "", type = TypeOfQuestion.RADIO, options = yesNoOptions),
  CHECK_ANSWERS(title = "", type = TypeOfQuestion.LONG_TEXT),
  COUNCIL_AREA(title = "", type = TypeOfQuestion.DROPDOWN, options = councilOptions),
  COUNCIL_AREA_REASON(title = "", type = TypeOfQuestion.LONG_TEXT),
}

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

interface AssessmentPage {
  val title: String
  val questions: List<ResettlementAssessmentQuestions>
}

enum class AttitudeAssessmentPage(override val title: String, override val questions: List<ResettlementAssessmentQuestions>) : AssessmentPage {
  ANGER_MANAGEMENT(title = "", questions = listOf()),
  ARE_THEY_INFLUENCED(title = "", questions = listOf()),
  ISSUES_WITH_GAMBLING(title = "", questions = listOf()),
  GANG_INVOLVEMENT(title = "", questions = listOf()),
  GANG_INVOLVEMENT_UNDER_THREAT(title = "", questions = listOf()),
  VICTIM_OF_SEXUAL_VIOLENCE(title = "", questions = listOf()),
  SEXUAL_VIOLENCE_DETAILS(title = "", questions = listOf()),
  VICTIM_OF_DOMESTIC_VIOLENCE(title = "", questions = listOf()),
  DOMESTIC_VIOLENCE_DETAILS(title = "", questions = listOf()),
}

enum class SharedPage (override val title: String, override val questions: List<ResettlementAssessmentQuestions>) : AssessmentPage {
  ASSESSMENT_SUMMARY(title = "[Pathway] assessment summary", questions = listOf())
}

enum class AccommodationAssessmentPage(override val title: String, override val questions: List<ResettlementAssessmentQuestions>) : AssessmentPage {
  WHERE_WILL_THEY_LIVE(title = "Where will they live when released from custody?", questions = listOf(AccommodationResettlementAssessmentQuestions.WHERE_WILL_THEY_LIVE, AccommodationResettlementAssessmentQuestions.WHAT_IS_THE_ADDRESS)),
  WHO_WILL_THEY_LIVE_WITH(title = "What are the names and ages of all residents at this property and the prisoner's relationship to them?", questions = listOf(AccommodationResettlementAssessmentQuestions.WHO_WILL_THEY_LIVE_WITH)),
  CONSENT_FOR_CRS(title = "Do they give consent for a Commissioned Rehabilitative Service (CRS)?", questions = listOf(AccommodationResettlementAssessmentQuestions.ACCOM_CRS)),
  WHAT_COUNCIL_AREA(title = "Which council area are they intending to move to on release?", questions = listOf(AccommodationResettlementAssessmentQuestions.COUNCIL_AREA, AccommodationResettlementAssessmentQuestions.COUNCIL_AREA_REASON)),
  CHECK_ANSWERS(title = "", questions = listOf()),
}

data class ResettlementAssessmentQuestion(
  val id: ResettlementAssessmentQuestions,
  val title: String,
  val type: TypeOfQuestion,
  val nextPage: (currentPage: AssessmentPage, questions: List<ResettlementAssessmentQuestionAndAnswer<*>>) -> AssessmentPage,
)

data class ResettlementAssessmentNode(
  val assessmentPage: AssessmentPage,
  val nextPage: (currentPage: AssessmentPage, questions: List<ResettlementAssessmentQuestionAndAnswer<*>>) -> AssessmentPage,
)

val yesNoOptions = listOf(
  Option(id = "YES", displayText = "Yes"),
  Option(id = "NO", displayText = "No"),
)

val councilOptions = listOf(
  Option(id = "CITY_OF_WESTMINSTER", displayText = "City of Westminster"),
  Option(id = "LONDON_BOROUGH_OF_CAMDEN", displayText = "London Borough of Camden"),
  Option(id = "MANCHESTER_CITY_COUNCIL", displayText = "Manchester City Council"),
  Option(id = "BIRMINGHAM_CITY_COUNCIL", displayText = "Birmingham City Council"),
  Option(id = "CARDIFF_CITY_COUNCIL", displayText = "Cardiff City Council"),
  Option(id = "LEEDS_CITY_COUNCIL", displayText = "Leeds City Council"),
  Option(id = "GLASGOW_CITY_COUNCIL", displayText = "Glasgow City Council"),
  Option(id = "BELFAST_CITY_COUNCIL", displayText = "Belfast City Council"),
  Option(id = "BRISTOL_CITY_COUNCIL", displayText = "Bristol City Council"),
  Option(id = "LIVERPOOL_CITY_COUNCIL", displayText = "Liverpool City Council"),
  Option(id = "SHEFFIELD_CITY_COUNCIL", displayText = "Sheffield City Council"),
  Option(id = "NEWCASTLE_CITY_COUNCIL", displayText = "Newcastle City Council"),
  Option(id = "NOTTINGHAM_CITY_COUNCIL", displayText = "Nottingham City Council"),
  Option(id = "CITY_OF_EDINBURGH_COUNCIL", displayText = "City of Edinburgh Council"),
  Option(id = "LEICESTER_CITY_COUNCIL", displayText = "Leicester City Council"),
  Option(id = "SOUTHAMPTON_CITY_COUNCIL", displayText = "Southampton City Council"),
  Option(id = "PLYMOUTH_CITY_COUNCIL", displayText = "Plymouth City Council"),
  Option(id = "COVENTRY_CITY_COUNCIL", displayText = "Coventry City Council"),
  Option(id = "NORWICH_CITY_COUNCIL", displayText = "Norwich City Council"),
  Option(id = "ABERDEEN_CITY_COUNCIL", displayText = "Aberdeen City Council"),
  Option(id = "BRIGHTON_AND_HOVE_CITY_COUNCIL", displayText = "Brighton and Hove City Council"),
  Option(id = "DERRY_CITY_AND_STRABANE_DISTRICT_COUNCIL", displayText = "Derry City and Strabane District Council"),
  Option(id = "PORTSMOUTH_CITY_COUNCIL", displayText = "Portsmouth City Council"),
  Option(id = "READING_BOROUGH_COUNCIL", displayText = "Reading Borough Council"),
  Option(id = "CITY_AND_COUNTY_OF_SWANSEA", displayText = "City and County of Swansea"),
  Option(id = "STOKE-ON-TRENT_CITY_COUNCIL", displayText = "Stoke-on-Trent City Council"),
  Option(id = "CITY_OF_YORK_COUNCIL", displayText = "City of York Council"),
  Option(id = "KINGSTON_UPON_HULL_CITY_COUNCIL", displayText = "Kingston upon Hull City Council"),
  Option(id = "OXFORD_CITY_COUNCIL", displayText = "Oxford City Council"),
  Option(id = "CITY_OF_SALFORD", displayText = "City of Salford"),
)
