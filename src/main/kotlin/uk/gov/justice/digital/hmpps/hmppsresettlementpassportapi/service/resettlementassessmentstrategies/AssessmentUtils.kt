package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.TagAndQuestionMapping
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
    profileTag = it.profileTag,
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
    )
  },
  validationType = this.validationType,
  customValidation = this.customValidation,
  detailsTitle = this.detailsTitle,
  detailsContent = this.detailsContent,
)

internal fun getProfileTag(questionId: String, answer: Answer<*>, pathway: Pathway): List<String> {
  val tagFound = mutableListOf<String>()
  TagAndQuestionMapping.entries.forEach {
    if ((it.questionId.contains(questionId)) &&
      (answer.answer.toString().contains(it.optionId)) &&
      (pathway.name == it.pathway.name)
    ) {
      tagFound.add(it.name)
    }
  }
  return tagFound.distinct()
}

internal fun processProfileTags(resettlementAssessmentEntity: ResettlementAssessmentEntity, pathway: Pathway): List<String> {
  val tagList = mutableListOf<String>()
  resettlementAssessmentEntity.assessment.assessment.forEach {
    val tagFound = getProfileTag(it.questionId, it.answer, pathway)
    if (tagFound.isNotEmpty()) {
      tagList.addAll(tagFound)
    }
  }
  return tagList
}
