package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseNotePathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayStatusAndCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Category
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ContactType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DeliusContactEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.DeliusContactRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import java.lang.IllegalArgumentException
import java.time.LocalDateTime

@Service
class DeliusContactService(private val deliusContactRepository: DeliusContactRepository, private val prisonerRepository: PrisonerRepository) {

  @Transactional
  fun addDeliusCaseNoteToDatabase(nomsId: String, pathwayStatusAndCaseNote: PathwayStatusAndCaseNote, username: String) {
    val prisonerEntity = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    deliusContactRepository.save(
      DeliusContactEntity(
        id = null,
        prisoner = prisonerEntity,
        category = Category.convertPathwayToCategory(pathwayStatusAndCaseNote.pathway),
        contactType = ContactType.CASE_NOTE,
        createdDate = LocalDateTime.now(),
        notes = pathwayStatusAndCaseNote.caseNoteText,
        createdBy = username,
      ),
    )
  }
  fun addAppointmentToDatabase(deliusContactEntity: DeliusContactEntity) {
    deliusContactRepository.save(deliusContactEntity)
    }

  fun getCaseNotesByNomsId(nomsId: String, pathwayType: CaseNotePathway): List<PathwayCaseNote> {
    val prisoner = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Prisoner with id $nomsId not found in database")
    val deliusContacts: List<DeliusContactEntity> = when (pathwayType) {
      CaseNotePathway.All -> deliusContactRepository.findByPrisoner(prisoner)
      CaseNotePathway.ACCOMMODATION -> deliusContactRepository.findByPrisonerAndCategory(prisoner, Category.ACCOMMODATION)
      CaseNotePathway.ATTITUDES_THINKING_AND_BEHAVIOUR -> deliusContactRepository.findByPrisonerAndCategory(prisoner, Category.ATTITUDES_THINKING_AND_BEHAVIOUR)
      CaseNotePathway.CHILDREN_FAMILIES_AND_COMMUNITY -> deliusContactRepository.findByPrisonerAndCategory(prisoner, Category.CHILDREN_FAMILIES_AND_COMMUNITY)
      CaseNotePathway.DRUGS_AND_ALCOHOL -> deliusContactRepository.findByPrisonerAndCategory(prisoner, Category.DRUGS_AND_ALCOHOL)
      CaseNotePathway.EDUCATION_SKILLS_AND_WORK -> deliusContactRepository.findByPrisonerAndCategory(prisoner, Category.EDUCATION_SKILLS_AND_WORK)
      CaseNotePathway.FINANCE_AND_ID -> deliusContactRepository.findByPrisonerAndCategory(prisoner, Category.FINANCE_AND_ID)
      CaseNotePathway.HEALTH -> deliusContactRepository.findByPrisonerAndCategory(prisoner, Category.HEALTH)
      CaseNotePathway.GENERAL -> emptyList()
    }
    return mapDeliusContactsToPathwayCaseNotes(deliusContacts)
  }

  fun mapDeliusContactsToPathwayCaseNotes(deliusContacts: List<DeliusContactEntity>): List<PathwayCaseNote> {
    return deliusContacts.map {
      PathwayCaseNote(
        caseNoteId = "db-${it.id}",
        pathway = convertCategoryToPathway(it.category),
        creationDateTime = it.createdDate,
        occurenceDateTime = it.createdDate,
        createdBy = it.createdBy,
        text = it.notes,
      )
    }
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
