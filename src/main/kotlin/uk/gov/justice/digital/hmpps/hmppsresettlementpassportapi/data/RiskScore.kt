package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import java.math.BigDecimal
import java.time.LocalDateTime

data class RiskScore(
  val completedDate: LocalDateTime?,
  val assessmentStatus: String?,
  val groupReconvictionScore: OgrScore?,
  val violencePredictorScore: OvpScore?,
  val generalPredictorScore: OgpScore?,
  val riskOfSeriousRecidivismScore: RsrScore?,
  val sexualPredictorScore: OspScore?,
)

data class OgrScore(
  val oneYear: BigDecimal?,
  val twoYears: BigDecimal?,
  val scoreLevel: String?,
)

data class OvpScore(
  val ovpStaticWeightedScore: BigDecimal?,
  val ovpDynamicWeightedScore: BigDecimal?,
  val ovpTotalWeightedScore: BigDecimal?,
  val oneYear: BigDecimal?,
  val twoYears: BigDecimal?,
  val ovpRisk: String?,
)

data class OgpScore(
  val ogpStaticWeightedScore: BigDecimal?,
  val ogpDynamicWeightedScore: BigDecimal?,
  val ogpTotalWeightedScore: BigDecimal?,
  val ogp1Year: BigDecimal?,
  val ogp2Year: BigDecimal?,
  val ogpRisk: String?,
)

data class RsrScore(
  val percentageScore: BigDecimal?,
  val staticOrDynamic: String?,
  val scoreLevel: String?,
)

class OspScore(
  val ospIndecentPercentageScore: BigDecimal?,
  val ospContactPercentageScore: BigDecimal?,
  val ospIndecentScoreLevel: String?,
  val ospContactScoreLevel: String?,
)
