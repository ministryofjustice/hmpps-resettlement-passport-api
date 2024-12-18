package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.MappaData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.RiskScore
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.RoshData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ArnApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService

@Service
class RiskService(
  private val arnApiService: ArnApiService,
  private val resettlementPassportDeliusApiService: ResettlementPassportDeliusApiService,
) {

  fun getRiskScoresByNomsId(nomsId: String): RiskScore {
    val crn = resettlementPassportDeliusApiService.getCrn(nomsId)
      ?: throw ResourceNotFoundException("Cannot find CRN for NomsId $nomsId in delius")
    return arnApiService.getRiskScoresByCrn(crn)
  }

  fun getRoshDataByNomsId(nomsId: String): RoshData {
    val crn = resettlementPassportDeliusApiService.getCrn(nomsId)
      ?: throw ResourceNotFoundException("Cannot find CRN for NomsId $nomsId in delius")
    return arnApiService.getRoshDataByCrn(crn)
  }

  fun getMappaDataByNomsId(nomsId: String): MappaData {
    val crn = resettlementPassportDeliusApiService.getCrn(nomsId)
    ?: throw ResourceNotFoundException("Cannot find CRN for NomsId $nomsId in delius")
    return resettlementPassportDeliusApiService.getMappaDataByCrn(crn)
  }
}
