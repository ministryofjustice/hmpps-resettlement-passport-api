package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.AccommodationAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.AccommodationResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository

@ExtendWith(MockitoExtension::class)
class ResettlementAssessmentServiceTest {

  private lateinit var resettlementAssessmentService: AccommodationResettlementAssessment
  private lateinit var prisonerRepository: PrisonerRepository
  private lateinit var pathwayRepository: PathwayRepository
  private lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository
  private lateinit var statusRepository: StatusRepository

  @BeforeEach
  fun beforeEach() {
    resettlementAssessmentService = AccommodationResettlementAssessment(resettlementAssessmentRepository, prisonerRepository, statusRepository, pathwayRepository)
  }

//  @Test
//  fun `test update pathway status`() {
//    var questions = mutableListOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
//      ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE", answer = StringAnswer("NEW_ADDRESS")),
//    )
//    var nextPage = resettlementAssessmentService.nextQuestions("WHERE_WILL_THEY_LIVE", questions)
//    Assertions.assertEquals(nextPage, AccommodationAssessmentPage.WHO_WILL_THEY_LIVE_WITH)
//
//    questions.add(
//      ResettlementAssessmentRequestQuestionAndAnswer(
//        "WHO_WILL_THEY_LIVE_WITH",
//        answer = ListAnswer(listOf("Joe Blogs, 47", "Jane Doe, 46")),
//      ),
//    )
//    nextPage = resettlementAssessmentService.nextQuestions("WHO_WILL_THEY_LIVE_WITH", questions)
//    Assertions.assertEquals(nextPage, AccommodationAssessmentPage.CHECK_ANSWERS)
//  }
}
