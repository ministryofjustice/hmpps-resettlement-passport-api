package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer

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
