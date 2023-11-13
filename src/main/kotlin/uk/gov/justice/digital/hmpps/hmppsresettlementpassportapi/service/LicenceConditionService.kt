package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.NoDataWithCodeFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.LicenceConditions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.CvlApiService

@Service
class LicenceConditionService(val cvlApiService: CvlApiService) {

  fun getLicenceConditionsByNomsId(nomsId: String): LicenceConditions? {
    val licence = cvlApiService.getLicenceByNomsId(nomsId) ?: throw NoDataWithCodeFoundException(
      "Prisoner",
      nomsId,
    )
    return cvlApiService.getLicenceConditionsByLicenceId(licence.licenceId)
  }

  fun getImageFromLicenceIdAndConditionId(licenceId: String, conditionId: String): ByteArray = cvlApiService.getImageFromLicenceIdAndConditionId(licenceId, conditionId)
}
