package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerSupportNeedRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.SupportNeedRepository
import java.time.LocalDateTime

@Service
class SupportNeedsLegacyProfileService(
  private val prisonerRepository: PrisonerRepository,
  private val supportNeedRepository: SupportNeedRepository,
  private val prisonerSupportNeedRepository: PrisonerSupportNeedRepository,
) {
  @Transactional
  fun setSupportNeedsLegacyFlag(nomsId: String, flag: Boolean) {
    val prisoner = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Unable to find prisoner $nomsId in database.")
    prisoner.supportNeedsLegacyProfile = flag
    prisonerRepository.save(prisoner)
  }

  @Transactional
  fun setSupportNeedsLegacyProfile() {
    prisonerRepository.updateProfileResetLegacyProfileFlags()
  }

  @Transactional
  fun addLegacySupportNeeds() {
    val legacySupportNeeds = supportNeedRepository.findAllBySectionAndDeletedIsFalse("Legacy support need")
    val legacyPrisoners = prisonerRepository.findAllBySupportNeedsLegacyProfileIsTrue()

    legacyPrisoners.forEach { prisoner ->
      val existingLegacySupportNeeds = prisonerSupportNeedRepository.countPrisonerSupportNeedEntityByPrisonerIdAndDeletedIsFalse(prisoner.id)
      // Only add legacy support needs if there are no needs currently set
      if (existingLegacySupportNeeds == 0) {
        val prisonerSupportNeeds = legacySupportNeeds.map { sn ->
          PrisonerSupportNeedEntity(
            prisonerId = prisoner.id!!,
            supportNeed = sn,
            otherDetail = null,
            createdBy = "System User",
            createdDate = LocalDateTime.now(),
          )
        }
        prisonerSupportNeedRepository.saveAll(prisonerSupportNeeds)
      }
    }
  }
}
