@file:OptIn(ExperimentalCoroutinesApi::class)

package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.netty.handler.codec.http.HttpHeaders.addHeader
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository

class CommunityApiServiceTest {

  private val mockWebServer: MockWebServer = MockWebServer()
  private lateinit var communityApiService: CommunityApiService

  @BeforeEach
  fun beforeEach() {
    mockWebServer.start()
    val webClient = WebClient.create(mockWebServer.url("/").toUrl().toString())
    val prisonerRepository: PrisonerRepository = mock()
    communityApiService = CommunityApiService(webClient, prisonerRepository)
  }

  @AfterEach
  fun afterEach() {
    mockWebServer.shutdown()
  }

  @Test
  fun `test get CRN happy path full json`() = runTest {
    val nomsId = "ABC1234"
    val expectedCrn = "D345678"

    val mockedJsonResponse = readFile("testdata/community-api/offender-details-valid-1.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))

    Assertions.assertEquals(expectedCrn, communityApiService.getCrn(nomsId))
  }

  @Test
  fun `test get CRN happy path min json`() = runTest {
    val nomsId = "ABC1234"
    val expectedCrn = "D345678"

    val mockedJsonResponse = readFile("testdata/community-api/offender-details-valid-2.json")
    mockWebServer.enqueue(MockResponse().setBody(mockedJsonResponse).addHeader("Content-Type", "application/json"))

    Assertions.assertEquals(expectedCrn, communityApiService.getCrn(nomsId))
  }

  @Test
  fun `test get CRN 404 error upstream`() = runTest {
    val nomsId = "ABC1234"

    mockWebServer.enqueue(MockResponse().setBody("{}").addHeader("Content-Type", "application/json").setResponseCode(404))
    assertThrows<ResourceNotFoundException> { communityApiService.getCrn(nomsId) }
  }

  @Test
  fun `test get CRN 500 error upstream`() = runTest {
    val nomsId = "ABC1234"

    mockWebServer.enqueue(MockResponse().setBody("{}").addHeader("Content-Type", "application/json").setResponseCode(500))
    assertThrows<WebClientResponseException> { communityApiService.getCrn(nomsId) }
  }
}
