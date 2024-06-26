package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.api

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.prisonapi.Prison
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

  @Test
  fun `test get active Prisons happy path full json`() {
    val expectedPrisonList = listOf(Prison(prisonId = "AKI", prisonName = "Acklington (HMP)", active = false, male = false, female = false, contracted = false, types = listOf(), addresses = listOf(), operators = listOf()), Prison(prisonId = "MDI", prisonName = "Moorland (HMP & YOI)", active = true, male = false, female = false, contracted = false, types = listOf(), addresses = listOf(), operators = listOf()), Prison(prisonId = "SWI", prisonName = "Swansea (HMP & YOI)", active = true, male = false, female = false, contracted = false, types = listOf(), addresses = listOf(), operators = listOf()))
    val mockedJsonResponse =
      readFile("testdata/prison-register-api/prison.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonList = prisonRegisterApiService.getPrisons()
    Assertions.assertEquals(expectedPrisonList, prisonList)
  }
}
