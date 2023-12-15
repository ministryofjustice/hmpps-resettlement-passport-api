package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
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
        prisoner = prisonerEntity,
        category = Category.convertPathwayToCategory(pathwayStatusAndCaseNote.pathway),
        contactType = ContactType.CASE_NOTE,
        createdDate = LocalDateTime.now(),
        notes = pathwayStatusAndCaseNote.caseNoteText,
        createdBy = username,
      ),
    )
  }

  fun addAppointmentToDatabase(deliusContactEntity: DeliusContactEntity){
    deliusContactRepository.save(deliusContactEntity)
  }
}
