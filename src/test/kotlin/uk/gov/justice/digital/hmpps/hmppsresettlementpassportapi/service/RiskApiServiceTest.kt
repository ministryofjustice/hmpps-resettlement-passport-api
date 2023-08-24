@file:OptIn(ExperimentalCoroutinesApi::class)

package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Category
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.RiskLevel
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.arnapi.RiskScoresDto
import java.time.LocalDateTime
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
  fun `test missing crn from database`() = runTest {
    val nomsId = "ABC1234"
    Mockito.`when`(communityApiService.findCrn(nomsId)).thenReturn(null)
    assertThrows<ResourceNotFoundException> { riskApiService.getRiskScoresByNomsId(nomsId) }
  }

  @ParameterizedTest
  @MethodSource("test convert to category to risk level map data")
  fun `test convert to category to risk level map`(
    inputMap: Map<String?, List<String>>,
    expectedMap: Map<Category, RiskLevel>,
  ) {
    Assertions.assertEquals(expectedMap, riskApiService.convertToCategoryToRiskLevelMap(inputMap))
  }

  private fun `test convert to category to risk level map data`(): Stream<Arguments> = Stream.of(
    // Happy path case 1
    Arguments.of(
      mapOf<String?, List<String>>(
        Pair("VERY_HIGH", listOf("Children", "Public")),
        Pair("HIGH", listOf("Known adult")),
        Pair("MEDIUM", listOf("Staff")),
        Pair("LOW", listOf("Prisoners")),
      ),
      mapOf(
        Pair(Category.CHILDREN, RiskLevel.VERY_HIGH),
        Pair(Category.PUBLIC, RiskLevel.VERY_HIGH),
        Pair(Category.KNOWN_ADULT, RiskLevel.HIGH),
        Pair(Category.STAFF, RiskLevel.MEDIUM),
        Pair(Category.PRISONERS, RiskLevel.LOW),
      ),
    ),
    // Happy path case 2
    Arguments.of(
      mapOf<String?, List<String>>(
        Pair("VERY_HIGH", listOf("Children")),
        Pair("HIGH", listOf("Staff")),
        Pair("MEDIUM", listOf("Known adult")),
        Pair("LOW", listOf("Prisoners", "Public")),
      ),
      mapOf(
        Pair(Category.CHILDREN, RiskLevel.VERY_HIGH),
        Pair(Category.STAFF, RiskLevel.HIGH),
        Pair(Category.KNOWN_ADULT, RiskLevel.MEDIUM),
        Pair(Category.PRISONERS, RiskLevel.LOW),
        Pair(Category.PUBLIC, RiskLevel.LOW),
      ),
    ),
    // No Data
    Arguments.of(
      mapOf<String?, List<String>>(),
      mapOf<Category, RiskLevel>(),
    ),
    // Nonsense Data
    Arguments.of(
      mapOf<String?, List<String>>(
        Pair("VERY_HIGH", listOf("string1", "string2", "string3")),
        Pair("HIGH", listOf("word")),
        Pair("MEDIUM", listOf("data")),
        Pair("LOW", listOf("hello", "world")),
      ),
      mapOf<Category, RiskLevel>(),
    ),
    // Happy path case - different casing
    Arguments.of(
      mapOf<String?, List<String>>(
        Pair("VERY HIGH", listOf("children")),
        Pair("High", listOf("STAFF")),
        Pair("medium", listOf("KNOWN_ADULT")),
        Pair("loW", listOf("Prisoners", "\"PUBLIC\"")),
      ),
      mapOf(
        Pair(Category.CHILDREN, RiskLevel.VERY_HIGH),
        Pair(Category.STAFF, RiskLevel.HIGH),
        Pair(Category.KNOWN_ADULT, RiskLevel.MEDIUM),
        Pair(Category.PRISONERS, RiskLevel.LOW),
        Pair(Category.PUBLIC, RiskLevel.LOW),
      ),
    ),
  )
}
