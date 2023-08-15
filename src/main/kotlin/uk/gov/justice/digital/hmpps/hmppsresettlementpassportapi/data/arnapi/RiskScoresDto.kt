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
  val scoreLevel: String?,
)

data class OvpScoreDto(
  val ovpStaticWeightedScore: BigDecimal?,
  val ovpDynamicWeightedScore: BigDecimal?,
  val ovpTotalWeightedScore: BigDecimal?,
  val oneYear: BigDecimal?,
  val twoYears: BigDecimal?,
  val ovpRisk: String?,
)

data class OgpScoreDto(
  val ogpStaticWeightedScore: BigDecimal?,
  val ogpDynamicWeightedScore: BigDecimal?,
  val ogpTotalWeightedScore: BigDecimal?,
  val ogp1Year: BigDecimal?,
  val ogp2Year: BigDecimal?,
  val ogpRisk: String?,
)

data class RsrScoreDto(
  val percentageScore: BigDecimal?,
  val staticOrDynamic: String?,
  val source: String,
  val algorithmVersion: String?,
  val scoreLevel: String?,
)

class OspScoreDto(
  val ospIndecentPercentageScore: BigDecimal?,
  val ospContactPercentageScore: BigDecimal?,
  val ospIndecentScoreLevel: String?,
  val ospContactScoreLevel: String?,
)
