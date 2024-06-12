package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LicenceConditions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LicenceConditionsResponse
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.LicenceConditionChangeAuditEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.LicenceConditionsChangeAuditRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.CvlApiService

@Service
class LicenceConditionService(
  val cvlApiService: CvlApiService,
  private val licenceConditionsChangeAuditRepository: LicenceConditionsChangeAuditRepository,
  private val prisonerRepository: PrisonerRepository,

) {

  fun getLicenceConditionsByNomsId(nomsId: String): LicenceConditionsResponse? {
    val licence = cvlApiService.getLicenceByNomsId(nomsId) ?: throw NoDataWithCodeFoundException(
      "Prisoner",
      nomsId,
    )
    val licenceConditions = cvlApiService.getLicenceConditionsByLicenceId(licence.licenceId)

    return LicenceConditionsResponse(licenceConditions, false)
  }

  @Transactional
  fun getLicenceConditionsAndUpdateAudit(nomsId: String): LicenceConditionsResponse? {
    val (licenceConditions, _) = getLicenceConditionsByNomsId(nomsId) ?: return null
    val changeStatus = compareAndSave(licenceConditions, nomsId)

    return LicenceConditionsResponse(licenceConditions, changeStatus)
  }

  fun getImageFromLicenceIdAndConditionId(licenceId: String, conditionId: String): ByteArray = cvlApiService.getImageFromLicenceIdAndConditionId(licenceId, conditionId)

  @Transactional
  fun compareAndSave(licenceConditions: LicenceConditions, nomsId: String): Boolean? {
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException(
        "Prisoner with id $nomsId not found",
      )
    val prisonerId = prisoner.id!!

    val licenceConditionsChangeAuditEntity = licenceConditionsChangeAuditRepository.findFirstByPrisonerIdOrderByCreationDateDesc(prisonerId)

    val existingLicenseConditions = licenceConditionsChangeAuditEntity?.licenceConditions
    println(licenceConditions)
    println(existingLicenseConditions)
    if (licenceConditionsChangeAuditEntity == null || existingLicenseConditions != licenceConditions) {
      val newLicenceConditionChangeAuditEntity = LicenceConditionChangeAuditEntity(
        prisonerId = prisonerId,
        licenceConditions = licenceConditions,
      )
      licenceConditionsChangeAuditRepository.save(newLicenceConditionChangeAuditEntity)
      return true
    }
    return false
  }
}
