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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.ClientCredentialsService.ServiceType

class CaseNotesClientCredentialsServiceTest {

  private val mockWebServer: MockWebServer = MockWebServer()
  private lateinit var clientCredentialsService: ClientCredentialsService

  @BeforeEach
  fun beforeEach() {
    mockWebServer.start()
    val webClient = WebClient.create(mockWebServer.url("/").toUrl().toString())
    clientCredentialsService = ClientCredentialsService(webClient, "https://case-notes-service-url", "https://arn-service-url")
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
        clientCredentialsService.getAuthorizedClient("fred", ServiceType.Arn)
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
      assertThat(clientCredentialsService.getAuthorizedClient("fred", ServiceType.CaseNotes)).isNotNull()
    }

    assertThat(mockWebServer.requestCount).isEqualTo(2)
  }
}
