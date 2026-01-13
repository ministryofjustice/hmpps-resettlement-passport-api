package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotePathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNoteType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayStatusAndCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Category
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ContactType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DeliusContactEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.DeliusContactRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.time.LocalDateTime

@Service
class DeliusContactService(private val deliusContactRepository: DeliusContactRepository, private val prisonerRepository: PrisonerRepository) {

  @Transactional
  fun addDeliusCaseNoteToDatabase(nomsId: String, pathwayStatusAndCaseNote: PathwayStatusAndCaseNote, username: String) {
    val prisonerEntity = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    deliusContactRepository.save(
      DeliusContactEntity(
        id = null,
        prisonerId = prisonerEntity.id(),
        category = Category.convertPathwayToCategory(pathwayStatusAndCaseNote.pathway),
        contactType = ContactType.CASE_NOTE,
        createdDate = LocalDateTime.now(),
        notes = pathwayStatusAndCaseNote.caseNoteText,
        createdBy = username,
      ),
    )
  }

  fun getCaseNotesByNomsId(nomsId: String, caseNoteType: CaseNoteType): List<PathwayCaseNote> {
    val prisoner = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val contactType = ContactType.CASE_NOTE
    val deliusContacts = when (caseNoteType) {
      CaseNoteType.All -> deliusContactRepository.findByPrisonerIdAndContactType(prisoner.id(), contactType)
      CaseNoteType.ACCOMMODATION -> deliusContactRepository.findByPrisonerIdAndContactTypeAndCategory(prisoner.id(), contactType, Category.ACCOMMODATION)
      CaseNoteType.ATTITUDES_THINKING_AND_BEHAVIOUR -> deliusContactRepository.findByPrisonerIdAndContactTypeAndCategory(prisoner.id(), contactType, Category.ATTITUDES_THINKING_AND_BEHAVIOUR)
      CaseNoteType.CHILDREN_FAMILIES_AND_COMMUNITY -> deliusContactRepository.findByPrisonerIdAndContactTypeAndCategory(prisoner.id(), contactType, Category.CHILDREN_FAMILIES_AND_COMMUNITY)
      CaseNoteType.DRUGS_AND_ALCOHOL -> deliusContactRepository.findByPrisonerIdAndContactTypeAndCategory(prisoner.id(), contactType, Category.DRUGS_AND_ALCOHOL)
      CaseNoteType.EDUCATION_SKILLS_AND_WORK -> deliusContactRepository.findByPrisonerIdAndContactTypeAndCategory(prisoner.id(), contactType, Category.EDUCATION_SKILLS_AND_WORK)
      CaseNoteType.FINANCE_AND_ID -> deliusContactRepository.findByPrisonerIdAndContactTypeAndCategory(prisoner.id(), contactType, Category.FINANCE_AND_ID)
      CaseNoteType.HEALTH -> deliusContactRepository.findByPrisonerIdAndContactTypeAndCategory(prisoner.id(), contactType, Category.HEALTH)
    }
    return mapDeliusContactsToPathwayCaseNotes(deliusContacts)
  }

  fun getAllCaseNotesByPrisonerIdAndCreationDate(
    prisonerId: Long,
    fromDate: LocalDateTime,
    toDate: LocalDateTime,
  ): List<PathwayCaseNoteSarContent> {
    val deliusContacts = deliusContactRepository.findByPrisonerIdAndContactTypeAndCreatedDateBetween(
      prisonerId,
      ContactType.CASE_NOTE,
      fromDate,
      toDate,
    )
    return mapDeliusContactsToSarContent(deliusContacts)
  }

  fun mapDeliusContactsToSarContent(deliusContacts: List<DeliusContactEntity>): List<PathwayCaseNoteSarContent> = deliusContacts.map {
    PathwayCaseNoteSarContent(
      pathway = convertCategoryToHumanReadablePathway(it.category),
      creationDateTime = it.createdDate,
      occurenceDateTime = it.createdDate,
      createdBy = convertFullNameToSurname(it.createdBy),
      text = it.notes,
    )
  }

  data class PathwayCaseNoteSarContent(
    val pathway: String,
    val creationDateTime: LocalDateTime,
    val occurenceDateTime: LocalDateTime,
    val createdBy: String,
    val text: String,
  )

  fun convertCategoryToHumanReadablePathway(category: Category) = when (category) {
    Category.ACCOMMODATION -> Pathway.ACCOMMODATION.displayName
    Category.ATTITUDES_THINKING_AND_BEHAVIOUR -> Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR.displayName
    Category.CHILDREN_FAMILIES_AND_COMMUNITY -> Pathway.CHILDREN_FAMILIES_AND_COMMUNITY.displayName
    Category.DRUGS_AND_ALCOHOL -> Pathway.DRUGS_AND_ALCOHOL.displayName
    Category.EDUCATION_SKILLS_AND_WORK -> Pathway.EDUCATION_SKILLS_AND_WORK.displayName
    Category.FINANCE_AND_ID -> Pathway.FINANCE_AND_ID.displayName
    Category.HEALTH -> Pathway.HEALTH.displayName
    Category.BENEFITS -> throw IllegalArgumentException("Error retrieving case notes - cannot use BENEFITS category with a case note") // should never happen
  }

  fun mapDeliusContactsToPathwayCaseNotes(deliusContacts: List<DeliusContactEntity>): List<PathwayCaseNote> = deliusContacts.map {
    PathwayCaseNote(
      caseNoteId = "db-${it.id}",
      pathway = convertCategoryToPathway(it.category),
      creationDateTime = it.createdDate,
      occurenceDateTime = it.createdDate,
      createdBy = it.createdBy,
      text = it.notes,
    )
  }

  fun convertCategoryToPathway(category: Category) = when (category) {
    Category.ACCOMMODATION -> CaseNotePathway.ACCOMMODATION
    Category.ATTITUDES_THINKING_AND_BEHAVIOUR -> CaseNotePathway.ATTITUDES_THINKING_AND_BEHAVIOUR
    Category.CHILDREN_FAMILIES_AND_COMMUNITY -> CaseNotePathway.CHILDREN_FAMILIES_AND_COMMUNITY
    Category.DRUGS_AND_ALCOHOL -> CaseNotePathway.DRUGS_AND_ALCOHOL
    Category.EDUCATION_SKILLS_AND_WORK -> CaseNotePathway.EDUCATION_SKILLS_AND_WORK
    Category.FINANCE_AND_ID -> CaseNotePathway.FINANCE_AND_ID
    Category.HEALTH -> CaseNotePathway.HEALTH
    Category.BENEFITS -> throw IllegalArgumentException("Error retrieving case notes - cannot use BENEFITS category with a case note") // should never happen
  }
}
