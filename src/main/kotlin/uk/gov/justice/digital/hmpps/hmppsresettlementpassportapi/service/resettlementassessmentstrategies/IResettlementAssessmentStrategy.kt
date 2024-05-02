package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentCompleteRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType

interface IResettlementAssessmentStrategy<Q : IResettlementAssessmentQuestion> {
  fun appliesTo(pathway: Pathway): Boolean
  fun getNextPageId(
    assessment: ResettlementAssessmentRequest,
    nomsId: String,
    pathway: Pathway,
    assessmentType: ResettlementAssessmentType,
    currentPage: String?,
  ): String
  fun completeAssessment(
    nomsId: String,
    pathway: Pathway,
    assessmentType: ResettlementAssessmentType,
    assessment: ResettlementAssessmentCompleteRequest,
    auth: String,
  )
  fun getPageFromId(nomsId: String, pathway: Pathway, pageId: String, assessmentType: ResettlementAssessmentType): ResettlementAssessmentResponsePage

  fun getQuestionById(id: String): IResettlementAssessmentQuestion
  fun findPageIdFromQuestionId(questionId: String, assessmentType: ResettlementAssessmentType): String
}
