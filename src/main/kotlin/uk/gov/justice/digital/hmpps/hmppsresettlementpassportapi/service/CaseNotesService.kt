package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotePathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotesMeta
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DpsCaseNotesSqsMessage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DpsCaseNoteEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.DpsCaseNoteRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.CaseNotesApiService
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.LocalDateTime
import java.util.Objects

@Service
class CaseNotesService(
  val caseNotesApiService: CaseNotesApiService,
  val deliusContactService: DeliusContactService,
  val dpsCaseNoteRepository: DpsCaseNoteRepository,
  val hmppsQueueService: HmppsQueueService,
  val objectMapper: ObjectMapper,
  val prisonerRepository: PrisonerRepository,
  @Value("\${case-notes.async.enabled:false}") val asyncEnabled: Boolean,
) {
  fun getCaseNotesByNomsId(nomsId: String, page: Int, size: Int, sort: String, days: Int, pathwayType: CaseNotePathway, createdByUserId: Int): CaseNotesList {
    if (page < 0 || size <= 0) {
      throw NoDataWithCodeFoundException(
        "Data",
        "Page $page and Size $size",
      )
    }

    if (nomsId.isBlank()) {
      throw NoDataWithCodeFoundException("Prisoner", nomsId)
    }

    var combinedCaseNotes = mutableListOf<PathwayCaseNote>()

    // Get case notes from DPS Case Notes API, delius_contact table and dps_case_notes
    combinedCaseNotes.addAll(caseNotesApiService.getCaseNotesByNomsId(nomsId, days, pathwayType, createdByUserId))
    if (createdByUserId == 0) { // RP2-900 For now for can't filter non-DPS case notes by the user. In this case just don't show anything from the database/Delius
      combinedCaseNotes.addAll(deliusContactService.getCaseNotesByNomsId(nomsId, pathwayType))
      combinedCaseNotes.addAll(getDpsCaseNotesFromDatabase(nomsId, pathwayType))
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

  fun getCaseNotesCreatorsByPathway(nomsId: String, pathwayType: CaseNotePathway): List<CaseNotesMeta> {
    return caseNotesApiService.getCaseNotesCreatorsByPathway(nomsId, pathwayType)
  }

  // Remove duplicates based on the createdBy + text + creationDate + occurrenceDate + pathway
  fun removeDuplicates(caseNotes: List<PathwayCaseNote>) = caseNotes.distinctBy { Objects.hash(it.createdBy, it.text, it.creationDateTime.toLocalDate(), it.occurenceDateTime.toLocalDate(), it.pathway) }.toMutableList()

  // New method of adding case notes to DPS via SQS
  fun addCaseNoteToDps(prisonerEntity: PrisonerEntity, pathwayEntity: PathwayEntity, createdDate: LocalDateTime, notes: String, name: String, userId: String) {
    if (asyncEnabled) {
      // Save case note to database
      val savedCaseNote = dpsCaseNoteRepository.save(
        DpsCaseNoteEntity(
          id = null,
          prisoner = prisonerEntity,
          pathway = pathwayEntity,
          createdDate = createdDate,
          notes = notes,
          createdBy = name,
        ),
      )

      val caseNoteId = savedCaseNote.id ?: throw RuntimeException("id for dps_case_note cannot be null")

      // Add case note to case notes SQS queue
      val sqsQueue =
        hmppsQueueService.findByQueueId("casenotes") ?: throw RuntimeException("Cannot find queue with id [casenotes]")
      sqsQueue.sqsClient.sendMessage {
        it.queueUrl(sqsQueue.queueUrl)
          .messageBody(objectMapper.writeValueAsString(DpsCaseNotesSqsMessage(caseNoteId, userId))).build()
      }
    } else {
      caseNotesApiService.postCaseNote(
        nomsId = prisonerEntity.nomsId,
        pathway = Pathway.getById(pathwayEntity.id),
        caseNotesText = notes,
        userId = userId,
      )
    }

    // TODO post to case notes.
  }

  fun getDpsCaseNotesFromDatabase(nomsId: String, pathwayType: CaseNotePathway): List<PathwayCaseNote> {
    val prisoner = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val dpsCaseNotes: List<DpsCaseNoteEntity> = when (pathwayType) {
      CaseNotePathway.All -> dpsCaseNoteRepository.findByPrisoner(prisoner)
      CaseNotePathway.ACCOMMODATION -> dpsCaseNoteRepository.findByPrisonerAndPathway(prisoner, Pathway.ACCOMMODATION.id)
      CaseNotePathway.ATTITUDES_THINKING_AND_BEHAVIOUR -> dpsCaseNoteRepository.findByPrisonerAndPathway(prisoner, Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR.id)
      CaseNotePathway.CHILDREN_FAMILIES_AND_COMMUNITY -> dpsCaseNoteRepository.findByPrisonerAndPathway(prisoner, Pathway.CHILDREN_FAMILIES_AND_COMMUNITY.id)
      CaseNotePathway.DRUGS_AND_ALCOHOL -> dpsCaseNoteRepository.findByPrisonerAndPathway(prisoner, Pathway.DRUGS_AND_ALCOHOL.id)
      CaseNotePathway.EDUCATION_SKILLS_AND_WORK -> dpsCaseNoteRepository.findByPrisonerAndPathway(prisoner, Pathway.EDUCATION_SKILLS_AND_WORK.id)
      CaseNotePathway.FINANCE_AND_ID -> dpsCaseNoteRepository.findByPrisonerAndPathway(prisoner, Pathway.FINANCE_AND_ID.id)
      CaseNotePathway.HEALTH -> dpsCaseNoteRepository.findByPrisonerAndPathway(prisoner, Pathway.HEALTH.id)
      CaseNotePathway.GENERAL -> emptyList()
    }
    return dpsCaseNotes.map {
      PathwayCaseNote(
        caseNoteId = "db-dps-${it.id}",
        pathway = convertPathwayToCaseNotePathway(Pathway.getById(it.pathway.id)),
        creationDateTime = it.createdDate,
        occurenceDateTime = it.createdDate,
        createdBy = it.createdBy,
        text = it.notes,
      )
    }
  }

  fun convertPathwayToCaseNotePathway(pathway: Pathway) = when (pathway) {
    Pathway.ACCOMMODATION -> CaseNotePathway.ACCOMMODATION
    Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR -> CaseNotePathway.ATTITUDES_THINKING_AND_BEHAVIOUR
    Pathway.CHILDREN_FAMILIES_AND_COMMUNITY -> CaseNotePathway.CHILDREN_FAMILIES_AND_COMMUNITY
    Pathway.DRUGS_AND_ALCOHOL -> CaseNotePathway.DRUGS_AND_ALCOHOL
    Pathway.EDUCATION_SKILLS_AND_WORK -> CaseNotePathway.EDUCATION_SKILLS_AND_WORK
    Pathway.FINANCE_AND_ID -> CaseNotePathway.FINANCE_AND_ID
    Pathway.HEALTH -> CaseNotePathway.HEALTH
  }
}
