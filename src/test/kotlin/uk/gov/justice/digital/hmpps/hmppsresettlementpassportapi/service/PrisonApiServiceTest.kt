package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.google.common.io.Resources
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient

class PrisonApiServiceTest {

  private val mockWebServer: MockWebServer = MockWebServer()
  private lateinit var prisonApiService: PrisonApiService

  @BeforeEach
  fun beforeEach() {
    mockWebServer.start()
    val webClient = WebClient.create(mockWebServer.url("/").toUrl().toString())
    prisonApiService = PrisonApiService(webClient)
  }

  @AfterEach
  fun afterEach() {
    mockWebServer.shutdown()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `test get active Prisons happy path full json`() = runTest {
    val expectedPrisonId = "SWI"
    val expectedPrisonName = "Swansea"
    val mockedJsonResponse =
      Resources.getResource("testdata/prison/prison.json").readText()
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonList = prisonApiService.getActivePrisonsList()
    Assertions.assertEquals(expectedPrisonId, prisonList[0].id)
    Assertions.assertEquals(expectedPrisonName, prisonList[0].name)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `test get all Prisons happy path full json`() = runTest {
    val expectedPrisonId = "AKI"
    val expectedPrisonName = "Acklington"
    val mockedJsonResponse =
      Resources.getResource("testdata/prison/prison.json").readText()
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonList = prisonApiService.getPrisonsList()
    Assertions.assertEquals(expectedPrisonId, prisonList[0].id)
    Assertions.assertEquals(expectedPrisonName, prisonList[0].name)
  }
}
