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
import org.mockito.kotlin.mock
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository

class OffenderSearchApiServiceTest {

  private val mockWebServer: MockWebServer = MockWebServer()
  private lateinit var offenderSearchApiService: OffenderSearchApiService

  @BeforeEach
  fun beforeEach() {
    mockWebServer.start()
    val webClient = WebClient.create(mockWebServer.url("/").toUrl().toString())
    val pathwayRepository: PathwayRepository = mock()
    offenderSearchApiService = OffenderSearchApiService(pathwayRepository, webClient)
  }

  @AfterEach
  fun afterEach() {
    mockWebServer.shutdown()
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `test get PrisonersList happy path full json with sort releaseDate Descending`() = runTest {
    val prisonId = "MDI"
    val expectedPrisonerId = "G6933GF"

    val mockedJsonResponse = Resources.getResource("testdata/prisoners/prisoner-offender-search.json").readText()
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId(false, prisonId, 0, 0, 10, "releaseDate,DESC")
    Assertions.assertEquals(expectedPrisonerId, prisonersList.content?.get(0)?.prisonerNumber ?: 0)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `test get PrisonersList happy path full json with sort releaseDate Ascending`() = runTest {
    val prisonId = "MDI"
    val expectedPrisonerId = "G6933GF"

    val mockedJsonResponse = Resources.getResource("testdata/prisoners/prisoner-offender-search.json").readText()
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId(false, prisonId, 0, 0, 10, "releaseDate,ASC")
    Assertions.assertEquals(
      expectedPrisonerId,
      prisonersList.content?.get((prisonersList.content!!.toList().size - 1))?.prisonerNumber
        ?: 0,
    )
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `test get PrisonersList happy path full json with sort firstName Ascending`() = runTest {
    val prisonId = "MDI"
    val expectedPrisonerId = "G6628UE"

    val mockedJsonResponse = Resources.getResource("testdata/prisoners/prisoner-offender-search.json").readText()
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId(false, prisonId, 0, 0, 10, "firstName,ASC")
    Assertions.assertEquals(expectedPrisonerId, prisonersList.content?.get(0)?.prisonerNumber ?: 0)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `test get PrisonersList happy path full json for page size`() = runTest {
    val prisonId = "MDI"
    val expectedPageSize = 5

    val mockedJsonResponse = Resources.getResource("testdata/prisoners/prisoner-offender-search.json").readText()
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId(false, prisonId, 0, 0, 5, "firstName,ASC")
    Assertions.assertEquals(expectedPageSize, prisonersList.pageSize)
    prisonersList.content?.toList()?.let { Assertions.assertEquals(expectedPageSize, it.size) }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun `test get PrisonersList happy path full json with date Range data and sort by release date desc`() = runTest {
    val prisonId = "MDI"
    val expectedPrisonerId = "G6933GF"

    val mockedJsonResponse = Resources.getResource("testdata/prisoners/prisoner-offender-search.json").readText()
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))
    val prisonersList = offenderSearchApiService.getPrisonersByPrisonId(true, prisonId, 1095, 0, 10, "releaseDate,DESC")
    Assertions.assertEquals(expectedPrisonerId, prisonersList.content?.get(0)?.prisonerNumber ?: 0)
  }
}
