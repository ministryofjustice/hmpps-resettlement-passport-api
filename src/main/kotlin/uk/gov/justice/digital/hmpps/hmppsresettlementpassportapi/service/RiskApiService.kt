package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Category
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.MappaData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.OgpScore
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.OgrScore
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.OspScore
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.OvpScore
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.RiskLevel
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.RiskScore
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.RoshData
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.RsrScore
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ScoreLevel
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.arnapi.AllRoshRiskDataDto
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.arnapi.RiskScoresDto

@Service
class RiskApiService(
  private val communityApiService: CommunityApiService,
  private val arnWebClientClientCredentials: WebClient,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun getRiskScoresByNomsId(prisonerId: String): RiskScore? {
    val crn = communityApiService.findCrn(prisonerId)
      ?: throw ResourceNotFoundException("Cannot find CRN for NomsId $prisonerId in database")

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
        convertStringToEnum(ScoreLevel::class, mostRecentRiskScoreDto.groupReconvictionScore?.scoreLevel),
      ),
      OvpScore(
        mostRecentRiskScoreDto.violencePredictorScore?.ovpStaticWeightedScore,
        mostRecentRiskScoreDto.violencePredictorScore?.ovpDynamicWeightedScore,
        mostRecentRiskScoreDto.violencePredictorScore?.ovpTotalWeightedScore,
        mostRecentRiskScoreDto.violencePredictorScore?.oneYear,
        mostRecentRiskScoreDto.violencePredictorScore?.twoYears,
        convertStringToEnum(ScoreLevel::class, mostRecentRiskScoreDto.violencePredictorScore?.ovpRisk),
      ),
      OgpScore(
        mostRecentRiskScoreDto.generalPredictorScore?.ogpStaticWeightedScore,
        mostRecentRiskScoreDto.generalPredictorScore?.ogpDynamicWeightedScore,
        mostRecentRiskScoreDto.generalPredictorScore?.ogpTotalWeightedScore,
        mostRecentRiskScoreDto.generalPredictorScore?.ogp1Year,
        mostRecentRiskScoreDto.generalPredictorScore?.ogp2Year,
        convertStringToEnum(ScoreLevel::class, mostRecentRiskScoreDto.generalPredictorScore?.ogpRisk),
      ),
      RsrScore(
        mostRecentRiskScoreDto.riskOfSeriousRecidivismScore?.percentageScore,
        mostRecentRiskScoreDto.riskOfSeriousRecidivismScore?.staticOrDynamic,
        convertStringToEnum(ScoreLevel::class, mostRecentRiskScoreDto.riskOfSeriousRecidivismScore?.scoreLevel),
      ),
      OspScore(
        mostRecentRiskScoreDto.sexualPredictorScore?.ospIndecentPercentageScore,
        mostRecentRiskScoreDto.sexualPredictorScore?.ospContactPercentageScore,
        convertStringToEnum(ScoreLevel::class, mostRecentRiskScoreDto.sexualPredictorScore?.ospIndecentScoreLevel),
        convertStringToEnum(ScoreLevel::class, mostRecentRiskScoreDto.sexualPredictorScore?.ospContactScoreLevel),
      ),
    )
  }

  fun List<RiskScoresDto>.getMostRecentRiskScore() = this.sortedBy { it.completedDate }.last()

  suspend fun getRoshDataByNomsId(prisonerId: String): RoshData? {
    val crn = communityApiService.findCrn(prisonerId)
      ?: throw ResourceNotFoundException("Cannot find CRN for NomsId $prisonerId in database")

    // Use CRN to get risk scores from ARN
    val allRoshRiskData = arnWebClientClientCredentials.get()
      .uri("/risks/crn/$crn")
      .retrieve()
      .onStatus({ it == HttpStatus.NOT_FOUND }, { throw ResourceNotFoundException("ARN service could not find CRN $crn/NomsId $prisonerId") })
      .awaitBody<AllRoshRiskDataDto>()

    val overallRiskLevel = convertStringToEnum(RiskLevel::class, allRoshRiskData.summary.overallRiskLevel)

    val categoryToRiskLevelMap = convertToCategoryToRiskLevelMap(allRoshRiskData.summary.riskInCommunity)

    return RoshData(
      categoryToRiskLevelMap,
      overallRiskLevel,
      allRoshRiskData.assessedOn,
    )
  }

  fun convertToCategoryToRiskLevelMap(riskInCommunitySummary: Map<String?, List<String>>): Map<Category, RiskLevel> {
    val categoryToRiskLevelMap = mutableMapOf<Category, RiskLevel>()
    riskInCommunitySummary.entries.forEach {
      it.value.forEach { cat ->
        val riskLevel = convertStringToEnum(RiskLevel::class, it.key!!)
        val category = convertStringToEnum(Category::class, cat)
        if (riskLevel != null && category != null) {
          categoryToRiskLevelMap[category] = riskLevel
        }
      }
    }

    return categoryToRiskLevelMap
  }

  suspend fun getMappaDataByNomsId(prisonerId: String): MappaData? = communityApiService.getMappaDataByNomsId(prisonerId)
}
