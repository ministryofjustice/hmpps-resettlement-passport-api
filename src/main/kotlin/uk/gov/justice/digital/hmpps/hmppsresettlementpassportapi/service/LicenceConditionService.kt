package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LicenceConditions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.LicenceConditionChangeAuditEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.LicenceConditionsChangeAuditRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.CvlApiService
import java.time.LocalDateTime

@Service
class LicenceConditionService(
  val cvlApiService: CvlApiService,
  private val licenceConditionsChangeAuditRepository: LicenceConditionsChangeAuditRepository,
  private val prisonerRepository: PrisonerRepository,

) {

  fun getLicenceConditionsByNomsId(nomsId: String): LicenceConditions? {
    val licence = cvlApiService.getLicenceByNomsId(nomsId) ?: throw NoDataWithCodeFoundException(
      "Prisoner",
      nomsId,
    )
    val licenceConditions = cvlApiService.getLicenceConditionsByLicenceId(licence.licenceId)

    val changeStatus = compareAndSave(licenceConditions.toString(), nomsId)
    licenceConditions.changeStatus = changeStatus
    return licenceConditions
  }

  fun getImageFromLicenceIdAndConditionId(licenceId: String, conditionId: String): ByteArray = cvlApiService.getImageFromLicenceIdAndConditionId(licenceId, conditionId)

  @Transactional
  fun compareAndSave(licenceConditions: String, nomsId: String): Boolean? {
    val prisoner = prisonerRepository.findByNomsId(nomsId)
      ?: throw ResourceNotFoundException(
        "Prisoner with id $nomsId not found",
      )

    val now = LocalDateTime.now()
    val hashedString = toMD5(licenceConditions)

    val licenceConditionsChangeAuditEntity = licenceConditionsChangeAuditRepository.findByPrisoner(prisoner)
    if (licenceConditionsChangeAuditEntity == null ||
      !licenceConditionsChangeAuditEntity.licenceConditionsJson.equals(hashedString)
    ) {
      val newLicenceConditionChangeAuditEntity = LicenceConditionChangeAuditEntity(
        null,
        prisoner,
        hashedString,
        creationDate = now,
      )
      if (licenceConditionsChangeAuditEntity != null) {
        licenceConditionsChangeAuditRepository.delete(licenceConditionsChangeAuditEntity)
      }
      licenceConditionsChangeAuditRepository.save(newLicenceConditionChangeAuditEntity)
      return true
    }
    return false
  }
}
