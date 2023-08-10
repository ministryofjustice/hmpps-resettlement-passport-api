package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.arnapi

import java.math.BigDecimal
import java.time.LocalDateTime

data class RiskScoresDto(
  val completedDate: LocalDateTime?,
  val assessmentStatus: String?,
  val groupReconvictionScore: OgrScoreDto?,
  val violencePredictorScore: OvpScoreDto?,
  val generalPredictorScore: OgpScoreDto?,
  val riskOfSeriousRecidivismScore: RsrScoreDto?,
  val sexualPredictorScore: OspScoreDto?,
)

data class OgrScoreDto(
  val oneYear: BigDecimal?,
  val twoYears: BigDecimal?,
  val scoreLevel: ScoreLevel?,
)

data class OvpScoreDto(
  val ovpStaticWeightedScore: BigDecimal?,
  val ovpDynamicWeightedScore: BigDecimal?,
  val ovpTotalWeightedScore: BigDecimal?,
  val oneYear: BigDecimal?,
  val twoYears: BigDecimal?,
  val ovpRisk: ScoreLevel?,
)

data class OgpScoreDto(
  val ogpStaticWeightedScore: BigDecimal?,
  val ogpDynamicWeightedScore: BigDecimal?,
  val ogpTotalWeightedScore: BigDecimal?,
  val ogp1Year: BigDecimal?,
  val ogp2Year: BigDecimal?,
  val ogpRisk: ScoreLevel?,
)

data class RsrScoreDto(
  val percentageScore: BigDecimal?,
  val staticOrDynamic: ScoreType?,
  val source: RsrScoreSource,
  val algorithmVersion: String?,
  val scoreLevel: ScoreLevel?,
)

class OspScoreDto(
  val ospIndecentPercentageScore: BigDecimal?,
  val ospContactPercentageScore: BigDecimal?,
  val ospIndecentScoreLevel: ScoreLevel?,
  val ospContactScoreLevel: ScoreLevel?,
)

enum class ScoreLevel {
  LOW, MEDIUM, HIGH, VERY_HIGH, NOT_APPLICABLE
}

enum class ScoreType {
  STATIC, DYNAMIC
}

enum class RsrScoreSource {
  ASSESSMENTS_API, OASYS, DELIUS;
}
