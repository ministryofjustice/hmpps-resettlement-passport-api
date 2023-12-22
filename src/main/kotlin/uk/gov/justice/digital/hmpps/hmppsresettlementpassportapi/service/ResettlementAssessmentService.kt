package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.*
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.ResettelmentAssessmentQuestions.AccommodationQuestions

@Service
class ResettlementAssessmentService {

  fun getNextQuestions(resettlementAssessment: ResettlementAssessment) {
    // Get pathway decision tree
    // identify current location in decision tree
    // find next questions in tree

  }

}

interface ResettlementAssessmentInterface {
  fun appliesTo(pathway: Pathway): Boolean
  fun nextQuestions(currentPage: AssessmentPage, questions: List<ResettlementAssessmentQuestionAndAnswer<*>>): AssessmentPage
}

class AccommodationResettlementAssessment : ResettlementAssessmentInterface {
  override fun appliesTo(pathway: Pathway): Boolean {
    return pathway == Pathway.ACCOMMODATION
  }

  override fun nextQuestions(currentPage: AssessmentPage, questions: List<ResettlementAssessmentQuestionAndAnswer<*>>): AssessmentPage {
    val questionLambda = AccommodationQuestions.accommodationQuestions.first { it.assessmentPage == currentPage}
    val nextPage = questionLambda.nextPage(currentPage, questions)
    return nextPage
  }
}

class AttitudesResettlementAssessment : ResettlementAssessmentInterface {
  override fun appliesTo(pathway: Pathway): Boolean {
    return pathway == Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR
  }

  override fun nextQuestions(currentPage: AssessmentPage, questions: List<ResettlementAssessmentQuestionAndAnswer<*>>): AssessmentPage {
    val questionLambda = AccommodationQuestions.accommodationQuestions.first { it.assessmentPage == currentPage}
    val nextPage = questionLambda.nextPage(currentPage, questions)
    return nextPage
  }
}