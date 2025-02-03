package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotePathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNoteType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesMeta
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DeliusCaseNoteType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DpsCaseNoteSubType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.CaseNotesApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import java.time.OffsetDateTime
import java.util.Objects

const val PROFILE_RESET_TEXT_PREFIX = "Prepare someone for release reports and statuses reset\n\nReason for reset: "
const val PROFILE_RESET_TEXT_SUFFIX = "\n\nAny previous immediate needs and pre-release reports have been saved in our archive, but are no longer visible in PSfR.\n\nAll pathway resettlement statuses have been set back to 'Not Started'."
const val PROFILE_RESET_TEXT_SUPPORT = "\n\nContact the service desk if you think there's a problem."

@Service
class CaseNotesService(
  val caseNotesApiService: CaseNotesApiService,
  val deliusContactService: DeliusContactService,
  val prisonerRepository: PrisonerRepository,
  val resettlementPassportDeliusApiService: ResettlementPassportDeliusApiService,
  val resettlementAssessmentRepository: ResettlementAssessmentRepository,
) {
  fun getCaseNotesByNomsId(nomsId: String, page: Int, size: Int, sort: String, days: Int, caseNoteType: CaseNoteType, createdByUserId: Int): CaseNotesList {
    if (page < 0 || size <= 0) {
      throw NoDataWithCodeFoundException(
        "Data",
        "Page $page and Size $size",
      )
    }

    if (nomsId.isBlank()) {
      throw NoDataWithCodeFoundException("Prisoner", nomsId)
    }
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    var combinedCaseNotes = mutableListOf<PathwayCaseNote>()

    // Get case notes from DPS Case Notes API, delius_contact table and dps_case_notes
    combinedCaseNotes.addAll(caseNotesApiService.getCaseNotesByNomsId(nomsId, days, caseNoteType, createdByUserId))
    if (createdByUserId == 0) { // RP2-900 For now for can't filter non-DPS case notes by the user. In this case just don't show anything from the database/Delius
      combinedCaseNotes.addAll(deliusContactService.getCaseNotesByNomsId(nomsId, caseNoteType))
    }
    combinedCaseNotes.addAll(getResettlementAssessmentCaseNotes(prisoner.id(), caseNoteType))
    if (caseNoteType !== CaseNoteType.All) {
      combinedCaseNotes.addAll(getProfileResetCaseNotes(nomsId, days, createdByUserId))
    }

    // Remove duplicates
    combinedCaseNotes = removeDuplicates(combinedCaseNotes)

    val sortValue = sort.ifBlank {
      "occurrenceDateTime,DESC"
    }

    when (sortValue) {
      // TODO remove spelling mistakes once UI is updated.
      "occurenceDateTime,ASC", "occurrenceDateTime,ASC" -> combinedCaseNotes.sortBy { it.occurenceDateTime }
      "pathway,ASC" -> combinedCaseNotes.sortBy { it.pathway }
      "occurenceDateTime,DESC", "occurrenceDateTime,DESC" -> combinedCaseNotes.sortByDescending { it.occurenceDateTime }
      "pathway,DESC" -> combinedCaseNotes.sortByDescending { it.pathway }
      else -> throw NoDataWithCodeFoundException(
        "Data",
        "Sort value Invalid",
      )
    }

    val startIndex = page * size
    if (combinedCaseNotes.size != 0 && startIndex >= combinedCaseNotes.size) {
      throw NoDataWithCodeFoundException(
        "Data",
        "Page $page",
      )
    }

    val endIndex = startIndex + size
    if (startIndex < endIndex && endIndex <= combinedCaseNotes.size) {
      val caseNotesPageList = combinedCaseNotes.subList(startIndex, endIndex)
      return CaseNotesList(
        caseNotesPageList,
        caseNotesPageList.size,
        page,
        sort,
        combinedCaseNotes.size,
        endIndex == combinedCaseNotes.size,
      )
    } else if (startIndex < endIndex) {
      val caseNotesPageList = combinedCaseNotes.subList(startIndex, combinedCaseNotes.size)
      return CaseNotesList(caseNotesPageList, caseNotesPageList.size, page, sort, combinedCaseNotes.size, true)
    }

    return CaseNotesList(null, null, null, null, 0, false)
  }

  fun sendProfileResetCaseNote(nomsId: String, userId: String, reason: String) {
    val noteText = PROFILE_RESET_TEXT_PREFIX + reason + PROFILE_RESET_TEXT_SUFFIX + PROFILE_RESET_TEXT_SUPPORT
    postBCSTCaseNoteToDps(nomsId, noteText, userId, DpsCaseNoteSubType.GEN)
  }

  private fun getProfileResetCaseNotes(nomsId: String, days: Int, createdByUserId: Int): List<PathwayCaseNote> = caseNotesApiService.getCaseNotesByNomsId(nomsId, days, CaseNoteType.All, createdByUserId).filter { it.text.startsWith(PROFILE_RESET_TEXT_PREFIX) }

  private fun getResettlementAssessmentCaseNotes(prisonerId: Long, caseNoteType: CaseNoteType): List<PathwayCaseNote> = when (caseNoteType) {
    CaseNoteType.All -> emptyList()
    CaseNoteType.ACCOMMODATION -> makePathwayCaseNotes(
      resettlementAssessmentRepository.findAllByPrisonerIdAndPathwayAndAssessmentStatus(
        prisonerId,
        Pathway.ACCOMMODATION,
      ),
    )

    CaseNoteType.HEALTH -> makePathwayCaseNotes(
      resettlementAssessmentRepository.findAllByPrisonerIdAndPathwayAndAssessmentStatus(
        prisonerId,
        Pathway.HEALTH,
      ),
    )

    CaseNoteType.FINANCE_AND_ID -> makePathwayCaseNotes(
      resettlementAssessmentRepository.findAllByPrisonerIdAndPathwayAndAssessmentStatus(
        prisonerId,
        Pathway.FINANCE_AND_ID,
      ),
    )

    CaseNoteType.CHILDREN_FAMILIES_AND_COMMUNITY -> makePathwayCaseNotes(
      resettlementAssessmentRepository.findAllByPrisonerIdAndPathwayAndAssessmentStatus(
        prisonerId,
        Pathway.CHILDREN_FAMILIES_AND_COMMUNITY,
      ),
    )

    CaseNoteType.ATTITUDES_THINKING_AND_BEHAVIOUR -> makePathwayCaseNotes(
      resettlementAssessmentRepository.findAllByPrisonerIdAndPathwayAndAssessmentStatus(
        prisonerId,
        Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR,
      ),
    )

    CaseNoteType.EDUCATION_SKILLS_AND_WORK -> makePathwayCaseNotes(
      resettlementAssessmentRepository.findAllByPrisonerIdAndPathwayAndAssessmentStatus(
        prisonerId,
        Pathway.EDUCATION_SKILLS_AND_WORK,
      ),
    )

    CaseNoteType.DRUGS_AND_ALCOHOL -> makePathwayCaseNotes(
      resettlementAssessmentRepository.findAllByPrisonerIdAndPathwayAndAssessmentStatus(
        prisonerId,
        Pathway.DRUGS_AND_ALCOHOL,
      ),
    )
  }

  private fun makePathwayCaseNotes(results: List<ResettlementAssessmentEntity>): List<PathwayCaseNote> = results.filter { it.statusChangedTo != null }
    .map { ra ->
      val id = "ResettlementAssessmentCaseNote${ra.id ?: throw RuntimeException("id missing from resettlement assessment entity!")}"
      val caseNotePathway = convertPathwayToCaseNotePathway(ra.pathway)
      val creationDate = ra.submissionDate ?: ra.creationDate
      val submissionDate = ra.submissionDate ?: ra.creationDate
      val createdBy = ra.createdBy
      val caseNoteText = ra.caseNoteText ?: "Resettlement status set to: ${ra.statusChangedTo!!.displayText}"
      PathwayCaseNote(id, caseNotePathway, creationDate, submissionDate, createdBy, caseNoteText)
    }

  private fun convertPathwayToCaseNotePathway(pathway: Pathway): CaseNotePathway = when (pathway) {
    Pathway.ACCOMMODATION -> CaseNotePathway.ACCOMMODATION
    Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR -> CaseNotePathway.ATTITUDES_THINKING_AND_BEHAVIOUR
    Pathway.CHILDREN_FAMILIES_AND_COMMUNITY -> CaseNotePathway.CHILDREN_FAMILIES_AND_COMMUNITY
    Pathway.DRUGS_AND_ALCOHOL -> CaseNotePathway.DRUGS_AND_ALCOHOL
    Pathway.EDUCATION_SKILLS_AND_WORK -> CaseNotePathway.EDUCATION_SKILLS_AND_WORK
    Pathway.FINANCE_AND_ID -> CaseNotePathway.FINANCE_AND_ID
    Pathway.HEALTH -> CaseNotePathway.HEALTH
  }

  fun getCaseNotesCreatorsByPathway(nomsId: String, caseNoteType: CaseNoteType): List<CaseNotesMeta> = caseNotesApiService.getCaseNotesCreatorsByPathway(nomsId, caseNoteType)

  // Remove duplicates based on the createdBy + text + creationDate + occurrenceDate + pathway
  fun removeDuplicates(caseNotes: List<PathwayCaseNote>) = caseNotes.distinctBy { Objects.hash(it.createdBy, it.text, it.creationDateTime.toLocalDate(), it.occurenceDateTime.toLocalDate(), it.pathway) }.toMutableList()

  fun postBCSTCaseNoteToDps(
    nomsId: String,
    notes: String,
    userId: String,
    subType: DpsCaseNoteSubType,
  ) {
    caseNotesApiService.postCaseNote(
      nomsId = nomsId,
      caseNotesText = notes,
      userId = userId,
      subType = subType,
    )
  }

  fun postBCSTCaseNoteToDelius(crn: String, prisonCode: String, notes: String, name: String, deliusCaseNoteType: DeliusCaseNoteType, description: String?): Boolean = resettlementPassportDeliusApiService.createCaseNote(
    crn = crn,
    type = deliusCaseNoteType,
    dateTime = OffsetDateTime.now(),
    notes = notes,
    author = convertFromNameToDeliusAuthor(prisonCode, name),
    description = description,

  )
}
