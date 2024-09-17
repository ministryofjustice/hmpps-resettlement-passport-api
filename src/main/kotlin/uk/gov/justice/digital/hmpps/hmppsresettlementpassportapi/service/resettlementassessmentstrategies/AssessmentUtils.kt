package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentOption
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity

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
    freeText = it.freeText,
    nestedQuestions = it.nestedQuestions?.map { nq -> nq.mapToResettlementAssessmentQuestionAndAnswer(originalPageId) },
    tag = it.tag,
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
    customValidation = this.customValidation,
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
      freeText = it.freeText,
      tag = it.tag,
    )
  },
  validationType = this.validationType,
  customValidation = this.customValidation,
  detailsTitle = this.detailsTitle,
  detailsContent = this.detailsContent,
)

internal fun getProfileTag(answer: Answer<*>, options: List<AssessmentConfigOption>): List<String> {
  val tagFound = mutableListOf<String>()

  options.forEach { opt ->
    if (answer.answer.toString().contains(opt.id)) {
      opt.tag?.let { tagFound.add(it) }
    }
  }
  return tagFound.distinct()
}

internal fun processProfileTags(resettlementAssessmentEntity: ResettlementAssessmentEntity, pages: List<AssessmentConfigPage>): List<String> {
  val tagList = mutableListOf<String>()
  resettlementAssessmentEntity.assessment.assessment.forEach {
    val options = findOptionsListFromQuestionId(it.questionId, pages)
    val tagFound = getProfileTag(it.answer, options)

    if (tagFound.isNotEmpty()) {
      tagList.addAll(tagFound)
    }
  }
  return tagList
}

internal fun findOptionsListFromQuestionId(
  questionId: String,
  pages: List<AssessmentConfigPage>,
): List<AssessmentConfigOption> {
  var optionsList = emptyList<AssessmentConfigOption>()

  pages.forEach { p ->
    p.questions?.forEach { q ->
      if (q.id == questionId && q.options != null) {
        optionsList = q.options
      }
    }
  }
  return optionsList
}
