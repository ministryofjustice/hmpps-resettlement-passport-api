package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoContentException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.BankApplicationResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.LatestResettlementAssessmentResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.PrisonerResettlementAssessment
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
    val startDate = fromDate ?: LocalDate.now().minusYears(50)
    val endDate = toDate ?: LocalDate.now()

    val prisonerEntity = prisonerRepository.findByNomsId(prn)
      ?: throw NoContentException("Prisoner with id $prn not found in database")
    val prisonerId = prisonerEntity.id!!

    val assessmentData = getAssessment(prn, startDate, endDate)
    val skippedAssessments = assessmentService.findSkippedAssessmentsForPrisoner(prisonerId, startDate, endDate)
    val bankApplicationData = getBankAccount(prn, startDate, endDate)
    val deliusContactData = getDeliusContact(prn, startDate, endDate)
    val idApplicationData = getId(prn, startDate, endDate)
    val pathwayStatus = pathwayAndStatusService.findAllPathwayStatusForPrisoner(prisonerEntity)
    val resettlementAssessmentsPathwayStatus = getPathwayStatus(prn, startDate, endDate)
    val resettlementAssessments = getResettlementAssessments(prn, startDate, endDate)
    val supportNeeds = supportNeedsService.getAllSupportNeedsForPrisoner(prisonerId, startDate, endDate)
    val supportNeedUpdates = supportNeedsService.getAllSupportNeedUpdatesForPrisoner(supportNeeds, startDate, endDate)
    val caseAllocation = caseAllocationService.getCaseAllocationHistoryByPrisonerId(prisonerId, startDate, endDate)
    val profileTags = resettlementAssessmentService.getProfileTagsByPrisonerId(prisonerId)
    val todoItems = todoService.getByPrisonerId(prisonerId, startDate, endDate)
    val documents = documentService.listDocuments(prisonerId, startDate, endDate)

    val resettlementData = ResettlementSarContent(
      prisonerEntity,
      assessmentData,
      skippedAssessments,
      bankApplicationData,
      deliusContactData,
      idApplicationData,
      pathwayStatus,
      resettlementAssessmentsPathwayStatus,
      resettlementAssessments,
      supportNeeds,
      supportNeedUpdates,
      caseAllocation,
      profileTags,
      todoItems,
      documents,
    )
    return HmppsSubjectAccessRequestContent(
      content = resettlementData,
    )
  }

  private fun getAssessment(prn: String, fromDate: LocalDate, toDate: LocalDate): AssessmentEntity? = runCatching {
    assessmentService.getAssessmentByNomsIdAndCreationDate(prn, fromDate, toDate)
  }.getOrNull()

  private fun getBankAccount(prn: String, fromDate: LocalDate, toDate: LocalDate): BankApplicationResponse? = runCatching {
    bankApplicationService.getBankApplicationByNomsIdAndCreationDate(prn, fromDate, toDate)
  }.getOrNull()

  private fun getDeliusContact(prn: String, fromDate: LocalDate, toDate: LocalDate): List<PathwayCaseNote>? = runCatching {
    deliusContactService.getAllCaseNotesByNomsIdAndCreationDate(prn, fromDate, toDate)
  }.getOrNull()

  private fun getId(prn: String, fromDate: LocalDate, toDate: LocalDate): IdApplicationEntity? = runCatching {
    idApplicationService.getIdApplicationByNomsIdAndCreationDate(prn, fromDate, toDate)
  }.getOrNull()

  private fun getPathwayStatus(
    prn: String,
    fromDate: LocalDate,
    toDate: LocalDate,
  ): List<ResettlementAssessmentPathwayStatus> = ResettlementAssessmentType.entries.mapNotNull { type ->
    runCatching {
      ResettlementAssessmentPathwayStatus(
        type,
        resettlementAssessmentService.getResettlementAssessmentSummaryByNomsIdAndCreationDate(
          prn,
          type,
          fromDate,
          toDate,
        ),
      )
    }.getOrNull()
  }

  private fun getResettlementAssessments(
    prn: String,
    fromDate: LocalDate,
    toDate: LocalDate,
  ): List<LatestResettlementAssessmentResponse> = Pathway.entries.mapNotNull { pathway ->
    runCatching {
      resettlementAssessmentService.getLatestResettlementAssessmentByNomsIdAndPathwayAndCreationDate(
        prn,
        pathway,
        resettlementAssessmentStrategies,
        fromDate,
        toDate,
      )
    }.getOrNull()
  }
}

data class ResettlementSarContent(
  val prisoner: PrisonerEntity?,
  val assessment: AssessmentEntity?,
  val skippedAssessments: List<AssessmentSkipEntity>?,
  val bankApplication: BankApplicationResponse?,
  val deliusContact: List<PathwayCaseNote>?,
  val idApplication: IdApplicationEntity?,
  val pathwayStatus: List<PathwayStatusEntity>?,
  val statusSummary: List<ResettlementAssessmentPathwayStatus>?,
  val resettlementAssessment: List<LatestResettlementAssessmentResponse>?,
  val supportNeeds: List<PrisonerSupportNeedEntity>?,
  val supportNeedUpdates: List<PrisonerSupportNeedUpdateEntity>?,
  val caseAllocation: List<CaseAllocationEntity>?,
  val profileTags: List<ProfileTagsEntity>?,
  val todoItems: List<TodoEntity>?,
  val documents: Collection<DocumentsEntity>?,
)

data class ResettlementAssessmentPathwayStatus(
  val type: ResettlementAssessmentType,
  val pathwayStatus: List<PrisonerResettlementAssessment>,
)
