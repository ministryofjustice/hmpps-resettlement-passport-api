package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoContentException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedUpdateEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.AssessmentService.AssessmentSarContent
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.AssessmentService.AssessmentSkipSarContent
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.BankApplicationService.BankApplicationSarContent
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.CaseAllocationService.CaseAllocationSarContent
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.DeliusContactService.PathwayCaseNoteSarContent
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.DocumentService.DocumentsSarContent
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.IdApplicationService.IdApplicationSarContent
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.PathwayAndStatusService.PathwayStatusSarContent
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.ResettlementAssessmentService.ProfileTagsSarContent
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.ResettlementAssessmentService.ResettlementAssessmentSarContent
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.TodoService.TodoSarContent
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
        prisoner = PrisonerSarContent(
          nomsId = prisoner.nomsId,
          creationDate = prisoner.creationDate,
          prisonId = prisoner.prisonId,
          supportNeedsLegacyProfile = prisoner.supportNeedsLegacyProfile,
        ),
        assessments = assessmentService.getAssessmentByPrisonerIdAndCreationDate(prisonerId, startDate, endDate),
        skippedAssessments = assessmentService.getSkippedAssessmentsForPrisoner(prisonerId, startDate, endDate),
        bankApplications = bankApplicationService.getBankApplicationsByPrisonerAndCreationDate(prisoner, startDate, endDate),
        deliusContacts = deliusContactService.getAllCaseNotesByPrisonerIdAndCreationDate(prisonerId, startDate, endDate),
        idApplications = idApplicationService.getIdApplicationByPrisonerIdAndCreationDate(prisonerId, startDate, endDate),
        pathwayStatus = pathwayAndStatusService.findAllPathwayStatusSarContentForPrisoner(prisoner),
        statusSummary = getPathwayStatus(prisonerId, startDate, endDate),
        resettlementAssessments = resettlementAssessmentService.getAllResettlementAssessmentsByPrisonerIdAndCreationDate(prisonerId, startDate, endDate, resettlementAssessmentStrategies),
        supportNeeds = getSupportNeedsSarContent(supportNeeds),
        supportNeedUpdates = getSupportNeedUpdatesSarContent(supportNeedsService.getAllSupportNeedUpdatesForPrisoner(supportNeeds, startDate, endDate)),
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
      assessmentType.displayName,
      pathwayStatus = resettlementAssessmentService.getResettlementAssessmentSummaryByPrisonerIdAndAssessmentTypeAndCreationDate(
        prisonerId,
        assessmentType,
        fromDate,
        toDate,
      ).map {
        PrisonerResettlementAssessmentSarContent(
          pathway = it.pathway.displayName,
          assessmentStatus = it.assessmentStatus.displayText,
        )
      },
    )
  }

  private fun getSupportNeedsSarContent(supportNeeds: List<PrisonerSupportNeedEntity>): List<PrisonerSupportNeedSarContent> = supportNeeds.map {
    PrisonerSupportNeedSarContent(
      SupportNeedSarContent(
        it.supportNeed.pathway.displayName,
        it.supportNeed.section,
        it.supportNeed.title,
        it.supportNeed.hidden,
        it.supportNeed.excludeFromCount,
        it.supportNeed.allowOtherDetail,
        it.supportNeed.createdDate,
      ),
      it.otherDetail,
      convertFullNameToSurname(it.createdBy),
      it.createdDate,
    )
  }

  private fun getSupportNeedUpdatesSarContent(supportNeedUpdates: List<PrisonerSupportNeedUpdateEntity>): List<PrisonerSupportNeedUpdateSarContent> = supportNeedUpdates.map {
    PrisonerSupportNeedUpdateSarContent(
      it.prisonerSupportNeedId,
      convertFullNameToSurname(it.createdBy),
      it.createdDate,
      it.updateText,
      it.status?.displayName,
      it.isPrison,
      it.isProbation,
    )
  }
}

data class ResettlementSarContent(
  val prisoner: PrisonerSarContent,
  val assessments: List<AssessmentSarContent>,
  val skippedAssessments: List<AssessmentSkipSarContent>,
  val bankApplications: List<BankApplicationSarContent>,
  val deliusContacts: List<PathwayCaseNoteSarContent>,
  val idApplications: List<IdApplicationSarContent>,
  val pathwayStatus: List<PathwayStatusSarContent>,
  val statusSummary: List<ResettlementAssessmentPathwayStatus>,
  val resettlementAssessments: List<ResettlementAssessmentSarContent>,
  val supportNeeds: List<PrisonerSupportNeedSarContent>,
  val supportNeedUpdates: List<PrisonerSupportNeedUpdateSarContent>,
  val caseAllocations: List<CaseAllocationSarContent>,
  val profileTags: List<ProfileTagsSarContent>,
  val todoItems: List<TodoSarContent>,
  val documents: List<DocumentsSarContent>,
)

data class PrisonerSarContent(
  val nomsId: String,
  val creationDate: LocalDateTime = LocalDateTime.now(),
  var prisonId: String?,
  var supportNeedsLegacyProfile: Boolean? = null,
)

data class ResettlementAssessmentPathwayStatus(
  val type: String,
  val pathwayStatus: List<PrisonerResettlementAssessmentSarContent>,
)

data class PrisonerResettlementAssessmentSarContent(
  val pathway: String,
  val assessmentStatus: String,
)

data class PrisonerSupportNeedSarContent(
  val supportNeed: SupportNeedSarContent,
  val otherDetail: String?,
  val createdBy: String,
  val createdDate: LocalDateTime,
)

data class SupportNeedSarContent(
  val pathway: String,
  val section: String?,
  val title: String,
  val hidden: Boolean,
  val excludeFromCount: Boolean,
  val allowOtherDetail: Boolean,
  val createdDate: LocalDateTime,
)

data class PrisonerSupportNeedUpdateSarContent(
  val prisonerSupportNeedId: Long,
  val createdBy: String,
  val createdDate: LocalDateTime,
  val updateText: String?,
  val status: String?,
  val isPrison: Boolean,
  val isProbation: Boolean,
)
