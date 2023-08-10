package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.OgpScore
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.OgrScore
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.OspScore
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.OvpScore
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.RiskScore
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.RsrScore
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.arnapi.RiskScoresDto

@Service
class RiskApiService(
  private val communityApiService: CommunityApiService,
  private val arnWebClientClientCredentials: WebClient,
) {
  suspend fun getRiskScoresByNomsId(prisonerId: String): RiskScore? {
    // Convert from NomsId to CRN - for now call the community API each time. In the future we may have this stored in the database.
    val crn = communityApiService.findCrn(prisonerId)
      ?: throw ResourceNotFoundException("Cannot find CRN for NomsId $prisonerId in Community API")

    // Use CRN to get risk scores from ARN
    val riskScoresDtoList = arnWebClientClientCredentials.get()
      .uri("/risks/crn/$crn/predictors/all")
      .retrieve()
      .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("ARN service could not find CRN $crn/NomsId $prisonerId") })
      .awaitBody<List<RiskScoresDto>>()

    if (riskScoresDtoList.isEmpty()) {
      throw ResourceNotFoundException("ARN returned no data for CRN $crn/NomsId $prisonerId")
    }

    val mostRecentRiskScoreDto = riskScoresDtoList.getMostRecentRiskScore()

    return RiskScore(
      mostRecentRiskScoreDto.completedDate,
      mostRecentRiskScoreDto.assessmentStatus,
      OgrScore(
        mostRecentRiskScoreDto.groupReconvictionScore?.oneYear,
        mostRecentRiskScoreDto.groupReconvictionScore?.twoYears,
        mostRecentRiskScoreDto.groupReconvictionScore?.scoreLevel?.name,
      ),
      OvpScore(
        mostRecentRiskScoreDto.violencePredictorScore?.ovpStaticWeightedScore,
        mostRecentRiskScoreDto.violencePredictorScore?.ovpDynamicWeightedScore,
        mostRecentRiskScoreDto.violencePredictorScore?.ovpTotalWeightedScore,
        mostRecentRiskScoreDto.violencePredictorScore?.oneYear,
        mostRecentRiskScoreDto.violencePredictorScore?.twoYears,
        mostRecentRiskScoreDto.violencePredictorScore?.ovpRisk?.name,
      ),
      OgpScore(
        mostRecentRiskScoreDto.generalPredictorScore?.ogpStaticWeightedScore,
        mostRecentRiskScoreDto.generalPredictorScore?.ogpDynamicWeightedScore,
        mostRecentRiskScoreDto.generalPredictorScore?.ogpTotalWeightedScore,
        mostRecentRiskScoreDto.generalPredictorScore?.ogp1Year,
        mostRecentRiskScoreDto.generalPredictorScore?.ogp2Year,
        mostRecentRiskScoreDto.generalPredictorScore?.ogpRisk?.name,
      ),
      RsrScore(
        mostRecentRiskScoreDto.riskOfSeriousRecidivismScore?.percentageScore,
        mostRecentRiskScoreDto.riskOfSeriousRecidivismScore?.staticOrDynamic?.name,
        mostRecentRiskScoreDto.riskOfSeriousRecidivismScore?.scoreLevel?.name,
      ),
      OspScore(
        mostRecentRiskScoreDto.sexualPredictorScore?.ospIndecentPercentageScore,
        mostRecentRiskScoreDto.sexualPredictorScore?.ospContactPercentageScore,
        mostRecentRiskScoreDto.sexualPredictorScore?.ospIndecentScoreLevel?.name,
        mostRecentRiskScoreDto.sexualPredictorScore?.ospContactScoreLevel?.name,
      ),
    )
  }

  fun List<RiskScoresDto>.getMostRecentRiskScore() = this.sortedBy { it.completedDate }.last()
}
