package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.ResettelmentAssessmentQuestions

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.AccommodationAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.AccommodationResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.AssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentNode
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentQuestionAndAnswer

val accommodationQuestions: List<ResettlementAssessmentNode> = listOf(
  ResettlementAssessmentNode(
    AccommodationAssessmentPage.WHERE_WILL_THEY_LIVE,
    nextPage =
    fun(currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>): AssessmentPage {
      if (currentQuestionsAndAnswers.any { it.question == AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE && it.answer?.answer is String && (it.answer?.answer as String) == "NO_PLACE_TO_LIVE" }) {
        return AccommodationAssessmentPage.CONSENT_FOR_CRS
      } else if (currentQuestionsAndAnswers.any {it.question == AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE && (it.answer?.answer as String == "PREVIOUS_ADDRESS" || it.answer?.answer as String == "NEW_ADDRESS") }) {
        return AccommodationAssessmentPage.WHO_WILL_THEY_LIVE_WITH
      }
      return AccommodationAssessmentPage.WHERE_WILL_THEY_LIVE //TODO figoue out error case
    },
  ),
  ResettlementAssessmentNode(
    AccommodationAssessmentPage.WHO_WILL_THEY_LIVE_WITH,
    nextPage =
    fun(_: List<ResettlementAssessmentQuestionAndAnswer>): AssessmentPage {
      return AccommodationAssessmentPage.CHECK_ANSWERS
    },
  ),
  ResettlementAssessmentNode(
    AccommodationAssessmentPage.CONSENT_FOR_CRS,
    nextPage =
    fun(_: List<ResettlementAssessmentQuestionAndAnswer>): AssessmentPage {
      return AccommodationAssessmentPage.WHAT_COUNCIL_AREA
    },
  ),
  ResettlementAssessmentNode(
    AccommodationAssessmentPage.WHAT_COUNCIL_AREA,
    nextPage =
    fun(_: List<ResettlementAssessmentQuestionAndAnswer>): AssessmentPage {
      return AccommodationAssessmentPage.CHECK_ANSWERS
    },
  ),
)
