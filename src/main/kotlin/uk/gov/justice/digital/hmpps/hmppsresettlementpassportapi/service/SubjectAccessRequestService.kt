package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoContentException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.BankApplicationResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.PrisonerResettlementAssessment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.AssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.AssessmentSkipEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.CaseAllocationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DocumentsEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.IdApplicationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedUpdateEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ProfileTagsEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.TodoEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies.ResettlementAssessmentStrategy
import uk.gov.justice.hmpps.kotlin.sar.HmppsPrisonSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class SubjectAccessRequestService(
  private val prisonerRepository: PrisonerRepository,
  private val assessmentService: AssessmentService,
  private val bankApplicationService: BankApplicationService,
  private val deliusContactService: DeliusContactService,
  private val idApplicationService: IdApplicationService,
  private val pathwayAndStatusService: PathwayAndStatusService,
  private val resettlementAssessmentService: ResettlementAssessmentService,
  private val resettlementAssessmentStrategies: ResettlementAssessmentStrategy,
  private val supportNeedsService: SupportNeedsService,
  private val caseAllocationService: CaseAllocationService,
  private val todoService: TodoService,
  private val documentService: DocumentService,
) : HmppsPrisonSubjectAccessRequestService {

  override fun getPrisonContentFor(
    prn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent {
    // Didn't seem to like the Max and Min values so picked 50 years ago and now
    val startDate = (fromDate ?: LocalDate.now().minusYears(50)).atStartOfDay()
    val endDate = (toDate ?: LocalDate.now()).atTime(LocalTime.MAX)

    val prisoner = prisonerRepository.findByNomsId(prn)
      ?: throw NoContentException("Prisoner with id $prn not found in database")
    val prisonerId = prisoner.id!!

    val supportNeeds = supportNeedsService.getAllSupportNeedsForPrisoner(prisonerId, startDate, endDate)

    return HmppsSubjectAccessRequestContent(
      content = ResettlementSarContent(
        prisoner = prisoner,
        assessments = assessmentService.getAssessmentByPrisonerIdAndCreationDate(prisonerId, startDate, endDate),
        skippedAssessments = assessmentService.getSkippedAssessmentsForPrisoner(prisonerId, startDate, endDate),
        bankApplications = bankApplicationService.getBankApplicationsByPrisonerAndCreationDate(prisoner, startDate, endDate),
        deliusContacts = deliusContactService.getAllCaseNotesByPrisonerIdAndCreationDate(prisonerId, startDate, endDate),
        idApplications = idApplicationService.getIdApplicationByPrisonerIdAndCreationDate(prisonerId, startDate, endDate),
        pathwayStatus = pathwayAndStatusService.findAllPathwayStatusForPrisoner(prisoner),
        statusSummary = getPathwayStatus(prisonerId, startDate, endDate),
        resettlementAssessments = resettlementAssessmentService.getAllResettlementAssessmentsByPrisonerIdAndCreationDate(prisonerId, startDate, endDate, resettlementAssessmentStrategies),
        supportNeeds = supportNeeds,
        supportNeedUpdates = supportNeedsService.getAllSupportNeedUpdatesForPrisoner(supportNeeds, startDate, endDate),
        caseAllocations = caseAllocationService.getCaseAllocationHistoryByPrisonerId(prisonerId, startDate, endDate),
        profileTags = resettlementAssessmentService.getProfileTagsByPrisonerId(prisonerId),
        todoItems = todoService.getByPrisonerId(prisonerId, startDate, endDate),
        documents = documentService.getDocuments(prisonerId, startDate, endDate),
      ),
    )
  }

  private fun getPathwayStatus(
    prisonerId: Long,
    fromDate: LocalDateTime,
    toDate: LocalDateTime,
  ): List<ResettlementAssessmentPathwayStatus> = ResettlementAssessmentType.entries.map { assessmentType ->
    ResettlementAssessmentPathwayStatus(
      assessmentType,
      pathwayStatus = resettlementAssessmentService.getResettlementAssessmentSummaryByPrisonerIdAndAssessmentTypeAndCreationDate(
        prisonerId,
        assessmentType,
        fromDate,
        toDate,
      ),
    )
  }
}

data class ResettlementSarContent(
  val prisoner: PrisonerEntity,
  val assessments: List<AssessmentEntity>,
  val skippedAssessments: List<AssessmentSkipEntity>,
  val bankApplications: List<BankApplicationResponse>,
  val deliusContacts: List<PathwayCaseNote>,
  val idApplications: List<IdApplicationEntity>,
  val pathwayStatus: List<PathwayStatusEntity>,
  val statusSummary: List<ResettlementAssessmentPathwayStatus>,
  val resettlementAssessments: List<ResettlementAssessmentResponse>,
  val supportNeeds: List<PrisonerSupportNeedEntity>,
  val supportNeedUpdates: List<PrisonerSupportNeedUpdateEntity>,
  val caseAllocations: List<CaseAllocationEntity>,
  val profileTags: List<ProfileTagsEntity>,
  val todoItems: List<TodoEntity>,
  val documents: List<DocumentsEntity>,
)

data class ResettlementAssessmentPathwayStatus(
  val type: ResettlementAssessmentType,
  val pathwayStatus: List<PrisonerResettlementAssessment>,
)
