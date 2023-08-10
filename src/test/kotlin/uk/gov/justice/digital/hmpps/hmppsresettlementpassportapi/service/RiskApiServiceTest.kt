@file:OptIn(ExperimentalCoroutinesApi::class)

package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.arnapi.RiskScoresDto
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class RiskApiServiceTest {

  @Mock
  private lateinit var communityApiService: CommunityApiService

  @Mock
  private lateinit var arnWebClientClientCredentials: WebClient

  private lateinit var riskApiService: RiskApiService

  @BeforeEach
  fun beforeEach() {
    riskApiService = RiskApiService(communityApiService, arnWebClientClientCredentials)
  }

  @Test
  fun `test getMostRecentRiskScore one item`() {
    val riskScoresDto = RiskScoresDto(LocalDateTime.parse("2023-07-29T03:07:38"), null, null, null, null, null, null)

    riskApiService.run {
      val riskScoresDtos = listOf(riskScoresDto)
      Assertions.assertEquals(riskScoresDto, riskScoresDtos.getMostRecentRiskScore())
    }
  }

  @Test
  fun `test getMostRecentRiskScore five items 1`() {
    val riskScoresDto1 = RiskScoresDto(LocalDateTime.parse("2023-07-29T03:07:38"), null, null, null, null, null, null)
    val riskScoresDto2 = RiskScoresDto(LocalDateTime.parse("2022-06-30T12:09:59"), null, null, null, null, null, null)
    val riskScoresDto3 = RiskScoresDto(LocalDateTime.parse("2023-08-09T15:34:28"), null, null, null, null, null, null)
    val riskScoresDto4 = RiskScoresDto(LocalDateTime.parse("2021-01-04T00:01:03"), null, null, null, null, null, null)
    val riskScoresDto5 = RiskScoresDto(LocalDateTime.parse("2023-08-09T15:34:29"), null, null, null, null, null, null)

    riskApiService.run {
      val riskScoresDtos = listOf(riskScoresDto1, riskScoresDto2, riskScoresDto3, riskScoresDto4, riskScoresDto5)
      Assertions.assertEquals(riskScoresDto5, riskScoresDtos.getMostRecentRiskScore())
    }
  }

  @Test
  fun `test getMostRecentRiskScore five items 2`() {
    val riskScoresDto1 = RiskScoresDto(LocalDateTime.parse("2021-01-04T00:01:03"), null, null, null, null, null, null)
    val riskScoresDto2 = RiskScoresDto(LocalDateTime.parse("2023-07-29T03:07:38"), null, null, null, null, null, null)
    val riskScoresDto3 = RiskScoresDto(LocalDateTime.parse("2022-06-30T12:09:59"), null, null, null, null, null, null)
    val riskScoresDto4 = RiskScoresDto(LocalDateTime.parse("2023-08-09T15:34:29"), null, null, null, null, null, null)
    val riskScoresDto5 = RiskScoresDto(LocalDateTime.parse("2023-08-09T15:34:28"), null, null, null, null, null, null)

    riskApiService.run {
      val riskScoresDtos = listOf(riskScoresDto1, riskScoresDto2, riskScoresDto3, riskScoresDto4, riskScoresDto5)
      Assertions.assertEquals(riskScoresDto4, riskScoresDtos.getMostRecentRiskScore())
    }
  }

  @Test
  fun `test no data from community api`() = runTest {
    val nomsId = "ABC1234"
    Mockito.`when`(communityApiService.findCrn(nomsId)).thenReturn(null)
    assertThrows<ResourceNotFoundException> { riskApiService.getRiskScoresByNomsId(nomsId) }
  }
}
