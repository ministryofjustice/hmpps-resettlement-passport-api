package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment

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
