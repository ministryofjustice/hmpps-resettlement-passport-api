package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.github.tomakehurst.wiremock.client.WireMock
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Category
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.RiskLevel
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ScoreLevel
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ArnApiService
import java.math.BigDecimal
import java.time.LocalDateTime

class ArnApiServiceIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var arnApiService: ArnApiService

  @BeforeEach
  override fun beforeEach() {
    cacheManager.cacheNames.forEach { cacheManager.getCache(it)?.clear() }
  }

  @Test
  fun `should receive risk scores by CRN`() {
    val crn = "CRN1"
    val responseJson = readFile("testdata/arn-api/crn-risk-predictors-1.json")

    arnApiMockServer.stubFor(
      WireMock.get(WireMock.urlEqualTo("/risks/crn/$crn/predictors/all"))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(responseJson),
        ),
    )

    val riskScore = arnApiService.getRiskScoresByCrn(crn)

    Assertions.assertNotNull(riskScore)
    Assertions.assertEquals(LocalDateTime.parse("2023-07-29T03:07:38"), riskScore?.completedDate)
    Assertions.assertEquals("string", riskScore?.assessmentStatus)

    // Assertions for OgrScore
    Assertions.assertNotNull(riskScore?.groupReconvictionScore)
    Assertions.assertEquals(BigDecimal.ZERO, riskScore?.groupReconvictionScore?.oneYear)
    Assertions.assertEquals(BigDecimal.ZERO, riskScore?.groupReconvictionScore?.twoYears)
    Assertions.assertEquals(ScoreLevel.LOW, riskScore?.groupReconvictionScore?.scoreLevel)

    // Assertions for OvpScore
    Assertions.assertNotNull(riskScore?.violencePredictorScore)
    Assertions.assertEquals(BigDecimal.ZERO, riskScore?.violencePredictorScore?.ovpStaticWeightedScore)
    Assertions.assertEquals(BigDecimal.ZERO, riskScore?.violencePredictorScore?.ovpDynamicWeightedScore)
    Assertions.assertEquals(BigDecimal.ZERO, riskScore?.violencePredictorScore?.ovpTotalWeightedScore)
    Assertions.assertEquals(BigDecimal.ZERO, riskScore?.violencePredictorScore?.oneYear)
    Assertions.assertEquals(BigDecimal.ZERO, riskScore?.violencePredictorScore?.twoYears)
    Assertions.assertEquals(ScoreLevel.LOW, riskScore?.violencePredictorScore?.ovpRisk)

    // Assertions for OgpScore
    Assertions.assertNotNull(riskScore?.generalPredictorScore)
    Assertions.assertEquals(BigDecimal.ZERO, riskScore?.generalPredictorScore?.ogpStaticWeightedScore)
    Assertions.assertEquals(BigDecimal.ZERO, riskScore?.generalPredictorScore?.ogpDynamicWeightedScore)
    Assertions.assertEquals(BigDecimal.ZERO, riskScore?.generalPredictorScore?.ogpTotalWeightedScore)
    Assertions.assertEquals(BigDecimal.ZERO, riskScore?.generalPredictorScore?.ogp1Year)
    Assertions.assertEquals(BigDecimal.ZERO, riskScore?.generalPredictorScore?.ogp2Year)
    Assertions.assertEquals(ScoreLevel.LOW, riskScore?.generalPredictorScore?.ogpRisk)

    // Assertions for RsrScore
    Assertions.assertNotNull(riskScore?.riskOfSeriousRecidivismScore)
    Assertions.assertEquals(BigDecimal.ZERO, riskScore?.riskOfSeriousRecidivismScore?.percentageScore)
    Assertions.assertEquals("STATIC", riskScore?.riskOfSeriousRecidivismScore?.staticOrDynamic)
    Assertions.assertEquals(ScoreLevel.LOW, riskScore?.riskOfSeriousRecidivismScore?.scoreLevel)

    // Assertions for OspScore
    Assertions.assertNotNull(riskScore?.sexualPredictorScore)
    Assertions.assertEquals(BigDecimal.ZERO, riskScore?.sexualPredictorScore?.ospIndecentPercentageScore)
    Assertions.assertEquals(BigDecimal.ZERO, riskScore?.sexualPredictorScore?.ospContactPercentageScore)
    Assertions.assertEquals(ScoreLevel.LOW, riskScore?.sexualPredictorScore?.ospIndecentScoreLevel)
    Assertions.assertEquals(ScoreLevel.LOW, riskScore?.sexualPredictorScore?.ospContactScoreLevel)
  }

  @Test
  fun `should receive rosh data by CRN`() {
    val crn = "CRN1"
    val responseJson = readFile("testdata/arn-api/crn-risks.json")

    arnApiMockServer.stubFor(
      WireMock.get(WireMock.urlEqualTo("/risks/crn/$crn"))
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(responseJson),
        ),
    )

    val roshData = arnApiService.getRoshDataByCrn(crn)

    Assertions.assertNotNull(roshData)

    // Assertions for categoryToRiskLevelMap
    val expectedRiskInCommunity = mapOf(
      Category.CHILDREN to RiskLevel.HIGH,
      Category.PUBLIC to RiskLevel.HIGH,
      Category.KNOWN_ADULT to RiskLevel.HIGH,
      Category.STAFF to RiskLevel.MEDIUM,
      Category.PRISONERS to RiskLevel.LOW,
    )
    Assertions.assertEquals(expectedRiskInCommunity.size, roshData?.riskInCommunity?.size)
    expectedRiskInCommunity.forEach { (category, expectedRiskLevel) ->
      Assertions.assertEquals(expectedRiskLevel, roshData?.riskInCommunity?.get(category))

      // Assertions for overallRiskLevel
      Assertions.assertEquals(RiskLevel.HIGH, roshData?.overallRiskLevel)

      // Assertions for assessedOn
      Assertions.assertEquals(LocalDateTime.parse("2023-07-29T03:07:38"), roshData?.assessedOn)
    }
  }
}
