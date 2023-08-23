package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.arnapi

import java.time.LocalDateTime

data class AllRoshRiskDataDto(
  val riskToSelf: RoshRiskToSelfDto,
  val otherRisks: OtherRoshRisksDto,
  val summary: RiskRoshSummaryDto,
  val assessedOn: LocalDateTime?,
)

data class RoshRiskToSelfDto(
  val suicide: RiskDto?,
  val selfHarm: RiskDto?,
  val custody: RiskDto?,
  val hostelSetting: RiskDto?,
  val vulnerability: RiskDto?,
  val assessedOn: LocalDateTime?,
)

data class OtherRoshRisksDto(
  val escapeOrAbscond: String?,
  val controlIssuesDisruptiveBehaviour: String?,
  val breachOfTrust: String?,
  val riskToOtherPrisoners: String?,
  val assessedOn: LocalDateTime?,
)

data class RiskRoshSummaryDto(
  val whoIsAtRisk: String?,
  val natureOfRisk: String?,
  val riskImminence: String?,
  val riskIncreaseFactors: String?,
  val riskMitigationFactors: String?,
  val riskInCommunity: Map<String?, List<String>>,
  val riskInCustody: Map<String?, List<String>>,
  val assessedOn: LocalDateTime?,
  val overallRiskLevel: String?,
)

data class RiskDto(
  val risk: String? = null,
  val previous: String? = null,
  val previousConcernsText: String? = null,
  val current: String? = null,
  val currentConcernsText: String? = null,
)
