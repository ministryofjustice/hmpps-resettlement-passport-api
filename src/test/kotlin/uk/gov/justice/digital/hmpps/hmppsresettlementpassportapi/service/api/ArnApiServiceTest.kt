package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.api

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Rule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Category
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.RiskLevel
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.arnapi.RiskScoresDto
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.ClientCredentialsService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ArnApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.jacksonCodecsConfigurer
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.stream.Stream
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration

class ArnApiServiceTest {

  @Rule
  @JvmField
  public val mockWebServer: MockWebServer = MockWebServer()

  private val clientCredentialsService = ClientCredentialsService(
    WebClient.builder().baseUrl(mockWebServer.url("/").toUrl().toString())
      .codecs(jacksonCodecsConfigurer)
      .build(),
    "https://case-notes-service-url",
    "https://arn-service-url",
  )

  private val arnApiService: ArnApiService = ArnApiService(clientCredentialsService, 10.milliseconds.toJavaDuration())

  @Test
  fun `times out when call to rosh api passes threshold`() {
    mockWebServer.enqueue(MockResponse().setResponseCode(404).setHeadersDelay(1, TimeUnit.SECONDS))

    assertThatThrownBy {
      arnApiService.getRoshDataByCrn("crn", "user1")
    }.isInstanceOf(TimeoutException::class.java)
  }

  @Test
  fun `times out when call to risk api passes threshold`() {
    mockWebServer.enqueue(MockResponse().setResponseCode(404).setHeadersDelay(1, TimeUnit.SECONDS))

    assertThatThrownBy {
      arnApiService.getRiskScoresByCrn("crn", "user1")
    }.isInstanceOf(TimeoutException::class.java)
  }

  @Test
  fun `test getMostRecentRiskScore one item`() {
    val riskScoresDto = RiskScoresDto(LocalDateTime.parse("2023-07-29T03:07:38"), null, null, null, null, null, null)

    arnApiService.run {
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

    arnApiService.run {
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

    arnApiService.run {
      val riskScoresDtos = listOf(riskScoresDto1, riskScoresDto2, riskScoresDto3, riskScoresDto4, riskScoresDto5)
      Assertions.assertEquals(riskScoresDto4, riskScoresDtos.getMostRecentRiskScore())
    }
  }

  @ParameterizedTest
  @MethodSource("test convert to category to risk level map data")
  fun `test convert to category to risk level map`(
    inputMap: Map<String?, List<String>>,
    expectedMap: Map<Category, RiskLevel>,
  ) {
    Assertions.assertEquals(expectedMap, arnApiService.convertToCategoryToRiskLevelMap(inputMap))
  }

  companion object {
    @JvmStatic
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
}
