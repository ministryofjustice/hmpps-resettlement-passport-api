package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository

@Service
class SupportNeedsLegacyProfileService(
  private val prisonerRepository: PrisonerRepository,
) {
  @Transactional
  fun setSupportNeedsLegacyFlag(nomsId: String, flag: Boolean) {
    val prisoner = prisonerRepository.findByNomsId(nomsId) ?: throw ResourceNotFoundException("Unable to find prisoner $nomsId in database.")
    prisoner.supportNeedsLegacyProfile = flag
    prisonerRepository.save(prisoner)
  }
}
