package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentOption
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ValidationType

internal fun convertFromSupportNeedAnswerToStatus(supportNeed: Answer<*>?): Status {
  if (supportNeed is StringAnswer) {
    return when (supportNeed.answer) {
      "SUPPORT_REQUIRED" -> Status.SUPPORT_REQUIRED
      "SUPPORT_NOT_REQUIRED" -> Status.SUPPORT_NOT_REQUIRED
      "SUPPORT_DECLINED" -> Status.SUPPORT_DECLINED
      "NOT_STARTED" -> Status.NOT_STARTED
      "IN_PROGRESS" -> Status.IN_PROGRESS
      "DONE" -> Status.DONE
      else -> throw ServerWebInputException("Support need [$supportNeed] is not a valid option")
    }
  } else {
    throw ServerWebInputException("Support need [$supportNeed] must be a StringAnswer")
  }
}

internal fun convertFromStringAnswer(answer: Answer<*>?): String {
  if (answer is StringAnswer) {
    return answer.answer ?: throw ServerWebInputException("Answer [$answer] must not be null")
  } else {
    throw ServerWebInputException("Answer [$answer] must be a StringAnswer")
  }
}

internal fun List<AssessmentConfigOption>?.mapToResettlementAssessmentOptions(originalPageId: String) = this?.map {
  ResettlementAssessmentOption(
    id = it.id,
    displayText = it.displayText,
    description = it.description,
    exclusive = it.exclusive,
    nestedQuestions = it.nestedQuestions?.map { nq -> nq.mapToResettlementAssessmentQuestionAndAnswer(originalPageId) },
  )
}

internal fun AssessmentConfigQuestion.mapToResettlementAssessmentQuestionAndAnswer(originalPageId: String): ResettlementAssessmentQuestionAndAnswer =
  ResettlementAssessmentQuestionAndAnswer(
    question = this.mapToResettlementAssessmentQuestion(originalPageId),
    originalPageId = originalPageId,
  )

internal fun AssessmentConfigQuestion.mapToResettlementAssessmentQuestion(originalPageId: String): ResettlementAssessmentQuestion =
  ResettlementAssessmentQuestion(
    id = this.id,
    title = this.title,
    subTitle = this.subTitle,
    type = this.type,
    options = this.options.mapToResettlementAssessmentOptions(originalPageId),
    validationType = this.validationType,
    validationRegex = this.validationRegex,
    detailsTitle = this.detailsTitle,
    detailsContent = this.detailsContent,
  )

internal fun List<AssessmentConfigQuestion>?.getFlattenedListOfQuestions() =
  this?.plus(this.filter { it.options != null }.flatMap { it.options!! }.filter { it.nestedQuestions != null }.flatMap { it.nestedQuestions!! }) ?: emptyList()

internal fun List<AssessmentConfigQuestion>?.getNestedQuestions() =
  this?.filter { it.options != null }?.flatMap { it.options!! }?.filter { it.nestedQuestions != null }?.flatMap { it.nestedQuestions!! } ?: emptyList()

internal fun ResettlementAssessmentQuestion.removeNestedQuestions() = ResettlementAssessmentQuestion(
  id = this.id,
  title = this.title,
  subTitle = this.subTitle,
  type = this.type,
  options = this.options?.map {
    ResettlementAssessmentOption(
      id = it.id,
      displayText = it.displayText,
      description = it.description,
      exclusive = it.exclusive,
      nestedQuestions = null,
    )
  },
  validationType = this.validationType,
  validationRegex = this.validationRegex,
  detailsTitle = this.detailsTitle,
  detailsContent = this.detailsContent,
)

fun validateAnswer(questionAndAnswer: ResettlementAssessmentQuestionAndAnswer) {
  if (questionAndAnswer.answer == null && questionAndAnswer.question.validationType == ValidationType.MANDATORY) {
    throw ServerWebInputException("No answer to mandatory question [${questionAndAnswer.question.id}] with regex validation")
  }
  if (questionAndAnswer.question.validationRegex != null) {
    // We must have a StringAnswer if there's a regex
    if (questionAndAnswer.answer is StringAnswer) {
      val regex = Regex(questionAndAnswer.question.validationRegex)
      val answer = (questionAndAnswer.answer as StringAnswer).answer
      if (answer != null) {
        if (!regex.matches(answer)) {
          throw ServerWebInputException("Invalid answer to question [${questionAndAnswer.question.id}] as failed to match regex [${questionAndAnswer.question.validationRegex}]")
        }
      }
    } else {
      throw ServerWebInputException("Invalid answer format to question [${questionAndAnswer.question.id}]. Must be a StringAnswer as regex validation is enabled.")
    }
  }
}
