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
    val crn = resettlementPassportDeliusApiService.findCrn(nomsId)
      ?: throw ResourceNotFoundException("Cannot find CRN for NomsId $nomsId in database")
    return arnApiService.getRiskScoresByCrn(crn)
  }

  fun getRoshDataByNomsId(nomsId: String): RoshData {
    val crn = resettlementPassportDeliusApiService.findCrn(nomsId)
      ?: throw ResourceNotFoundException("Cannot find CRN for NomsId $nomsId in database")
    return arnApiService.getRoshDataByCrn(crn)
  }

  fun getMappaDataByNomsId(nomsId: String): MappaData = resettlementPassportDeliusApiService.getMappaDataByNomsId(nomsId)
}
