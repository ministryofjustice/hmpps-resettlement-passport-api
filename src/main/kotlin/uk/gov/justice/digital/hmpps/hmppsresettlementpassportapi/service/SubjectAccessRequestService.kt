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
  private val idApplicationService: IdApplicationService,
  private val pathwayAndStatusService: PathwayAndStatusService,
  private val resettlementAssessmentService: ResettlementAssessmentService,
  private val resettlementAssessmentStrategies: ResettlementAssessmentStrategy,
  private val supportNeedsService: SupportNeedsService,
  private val caseAllocationService: CaseAllocationService,
  private val todoService: TodoService,
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

    val supportNeeds = supportNeedsService.getAllSupportNeedsBytPrisonerIdForSAR(prisonerId, startDate, endDate)

    return HmppsSubjectAccessRequestContent(
      content = ResettlementSarContent(
        prisoner = PrisonerSarContent(
          nomsId = prisoner.nomsId,
          creationDate = prisoner.creationDate,
          prisonId = prisoner.prisonId,
        ),
        assessments = assessmentService.getAssessmentByPrisonerIdForSAR(prisonerId, startDate, endDate),
        skippedAssessments = assessmentService.getSkippedAssessmentsByPrisonerIdForSAR(prisonerId, startDate, endDate),
        bankApplications = bankApplicationService.getBankApplicationsByPrisonerIdForSAR(prisonerId, startDate, endDate),
        idApplications = idApplicationService.getIdApplicationByPrisonerIdForSAR(prisonerId, startDate, endDate),
        pathwayStatus = pathwayAndStatusService.findAllPathwayStatusByPrisonerIdForSAR(prisonerId),
        statusSummary = getPathwayStatus(prisonerId, startDate, endDate),
        resettlementAssessments = resettlementAssessmentService.getAllResettlementAssessmentsByPrisonerIdForSAR(prisonerId, startDate, endDate, resettlementAssessmentStrategies),
        supportNeeds = supportNeeds.supportNeedsSarContent(),
        supportNeedUpdates = supportNeedsService.getAllSupportNeedUpdatesBySupportNeedsForSAR(supportNeeds, startDate, endDate).supportNeedUpdatesSarContent(),
        caseAllocations = caseAllocationService.getCaseAllocationHistoryByPrisonerIdForSAR(prisonerId, startDate, endDate),
        profileTags = resettlementAssessmentService.getProfileTagsByPrisonerIdForSAR(prisonerId),
        todoItems = todoService.getByPrisonerIdForSAR(prisonerId, startDate, endDate),
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

  private fun List<PrisonerSupportNeedEntity>.supportNeedsSarContent() = map {
    PrisonerSupportNeedSarContent(
      SupportNeedSarContent(
        it.supportNeed.pathway.displayName,
        it.supportNeed.section,
        it.supportNeed.title,
        it.supportNeed.createdDate,
      ),
      it.otherDetail,
      convertFullNameToSurname(it.createdBy),
      it.createdDate,
    )
  }

  private fun List<PrisonerSupportNeedUpdateEntity>.supportNeedUpdatesSarContent() = map {
    PrisonerSupportNeedUpdateSarContent(
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
  val idApplications: List<IdApplicationSarContent>,
  val pathwayStatus: List<PathwayStatusSarContent>,
  val statusSummary: List<ResettlementAssessmentPathwayStatus>,
  val resettlementAssessments: List<ResettlementAssessmentSarContent>,
  val supportNeeds: List<PrisonerSupportNeedSarContent>,
  val supportNeedUpdates: List<PrisonerSupportNeedUpdateSarContent>,
  val caseAllocations: List<CaseAllocationSarContent>,
  val profileTags: List<ProfileTagsSarContent>,
  val todoItems: List<TodoSarContent>,
)

data class PrisonerSarContent(
  val nomsId: String,
  val creationDate: LocalDateTime = LocalDateTime.now(),
  var prisonId: String?,
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
  val createdDate: LocalDateTime,
)

data class PrisonerSupportNeedUpdateSarContent(
  val createdBy: String,
  val createdDate: LocalDateTime,
  val updateText: String?,
  val status: String?,
  val isPrison: Boolean,
  val isProbation: Boolean,
)
