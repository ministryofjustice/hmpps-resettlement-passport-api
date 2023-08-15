package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import java.time.LocalDateTime

data class RoshData(
  val riskInCommunity: Map<String, RiskLevel>,
  val overallRiskLevel: RiskLevel?,
  val assessedOn: LocalDateTime?
)

enum class RiskLevel {
  VERY_HIGH, HIGH, MEDIUM, LOW;
}
