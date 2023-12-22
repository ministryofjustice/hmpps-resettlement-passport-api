package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.*
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status

@ExtendWith(MockitoExtension::class)
class ResettlementAssessmentServiceTest {

  private lateinit var resettlementAssessmentService: AccommodationResettlementAssessment

  @BeforeEach
  fun beforeEach() {
    resettlementAssessmentService = AccommodationResettlementAssessment()
  }

  @Test
  fun `test update pathway status`() {
    var questions = mutableListOf<ResettlementAssessmentQuestionAndAnswer<*>>(
      ResettlementAssessmentQuestionAndAnswer(ResettlementAssessmentQuestions.WHERE_WILL_THEY_LIVE, answer = "NEW_ADDRESS",)
    )
    var nextPage = resettlementAssessmentService.nextQuestions(AccommodationAssessmentPage.WHERE_WILL_THEY_LIVE, questions)
    Assertions.assertEquals(nextPage, AccommodationAssessmentPage.WHO_WILL_THEY_LIVE_WITH)

    questions.add(ResettlementAssessmentQuestionAndAnswer(
      ResettlementAssessmentQuestions.WHO_WILL_THEY_LIVE_WITH, answer = listOf("Joe Blogs, 47", "Jane Doe, 46"))
    )
    nextPage = resettlementAssessmentService.nextQuestions(AccommodationAssessmentPage.WHO_WILL_THEY_LIVE_WITH, questions)
    Assertions.assertEquals(nextPage, AccommodationAssessmentPage.CHECK_ANSWERS)
  }
}