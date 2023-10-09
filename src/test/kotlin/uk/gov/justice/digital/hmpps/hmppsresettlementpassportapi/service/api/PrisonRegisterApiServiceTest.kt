package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.api

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Prison
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonRegisterApiService

class PrisonRegisterApiServiceTest {

  private val mockWebServer: MockWebServer = MockWebServer()
  private lateinit var prisonRegisterApiService: PrisonRegisterApiService

  @BeforeEach
  fun beforeEach() {
    mockWebServer.start()
    val webClient = WebClient.create(mockWebServer.url("/").toUrl().toString())
    prisonRegisterApiService = PrisonRegisterApiService(webClient)
  }

  @AfterEach
  fun afterEach() {
    mockWebServer.shutdown()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `test get active Prisons happy path full json`() = runTest {
    val expectedPrisonList = listOf(Prison("MDI", "Moorland (HMP & YOI)", true), Prison("SWI", "Swansea (HMP & YOI)", true))
    val mockedJsonResponse =
      readFile("testdata/prison-register-api/prison.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonList = prisonRegisterApiService.getActivePrisonsList()
    Assertions.assertEquals(expectedPrisonList, prisonList)
  }
}
