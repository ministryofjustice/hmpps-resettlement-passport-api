package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

class CaseNotesClientCredentialsServiceTest {

  private val mockWebServer: MockWebServer = MockWebServer()
  private lateinit var caseNotesClientCredentialsService: CaseNotesClientCredentialsService

  @BeforeEach
  fun beforeEach() {
    mockWebServer.start()
    val webClient = WebClient.create(mockWebServer.url("/").toUrl().toString())
    caseNotesClientCredentialsService = CaseNotesClientCredentialsService(webClient, "https://case-notes-service-url")
  }

  @AfterEach
  fun afterEach() {
    mockWebServer.shutdown()
  }

  @Test
  fun `retries on error`() {
    mockWebServer.enqueue(MockResponse().setResponseCode(500))
    mockWebServer.enqueue(MockResponse().setResponseCode(500))
    mockWebServer.enqueue(MockResponse().setResponseCode(500))

    assertThatThrownBy {
      runBlocking {
        caseNotesClientCredentialsService.getAuthorizedClient("fred")
      }
    }.isInstanceOf(WebClientResponseException.InternalServerError::class.java)

    assertThat(mockWebServer.requestCount).isEqualTo(3)
  }

  @Test
  fun `retries and then succeeds`() {
    mockWebServer.enqueue(MockResponse().setResponseCode(500))
    mockWebServer.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody("""{ "access_token": "token" }""")
        .addHeader("Content-Type", "application/json"),
    )
    mockWebServer.enqueue(MockResponse().setResponseCode(500))

    runBlocking {
      assertThat(caseNotesClientCredentialsService.getAuthorizedClient("fred")).isNotNull()
    }

    assertThat(mockWebServer.requestCount).isEqualTo(2)
  }
}
