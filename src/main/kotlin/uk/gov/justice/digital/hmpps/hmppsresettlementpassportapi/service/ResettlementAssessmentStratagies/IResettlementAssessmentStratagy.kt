package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.ResettlementAssessmentStratagies

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway

interface IResettlementAssessmentStrategy {
  fun appliesTo(pathway: Pathway): Boolean
  fun storeAssessment(assessment: ResettlementAssessmentRequest, auth: String)
  fun nextQuestions(assessment: ResettlementAssessmentRequest): IAssessmentPage
}