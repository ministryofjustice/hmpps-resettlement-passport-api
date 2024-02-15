package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentNode
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository

@Service
class HealthResettlementAssessmentStrategy(
  resettlementAssessmentRepository: ResettlementAssessmentRepository,
  prisonerRepository: PrisonerRepository,
  statusRepository: StatusRepository,
  pathwayRepository: PathwayRepository,
  resettlementAssessmentStatusRepository: ResettlementAssessmentStatusRepository,
) : AbstractResettlementAssessmentStrategy<HealthAssessmentPage, HealthResettlementAssessmentQuestion>(resettlementAssessmentRepository, prisonerRepository, statusRepository, pathwayRepository, resettlementAssessmentStatusRepository, HealthAssessmentPage::class, HealthResettlementAssessmentQuestion::class) {
  override fun appliesTo(pathway: Pathway) = pathway == Pathway.HEALTH

  override fun getPageList(): List<ResettlementAssessmentNode> = emptyList() // TODO Add page list
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class HealthAssessmentPage(override val id: String, override val questionsAndAnswers: MutableList<ResettlementAssessmentQuestionAndAnswer>) :
  IAssessmentPage // TODO Add pages

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class HealthResettlementAssessmentQuestion(
  override val id: String,
  override val title: String,
  override val subTitle: String? = null,
  override val type: TypeOfQuestion,
  override val options: List<Option>? = null,
) : IResettlementAssessmentQuestion // TODO Add questions
