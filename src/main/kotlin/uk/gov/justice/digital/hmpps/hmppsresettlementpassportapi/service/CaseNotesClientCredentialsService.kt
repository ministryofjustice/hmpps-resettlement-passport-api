package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Service
class CaseNotesClientCredentialsService(
  val tokenWebClient: WebClient,
  @Value("\${api.base.url.case-notes}") private val caseNotesRootUri: String,
) {

  private suspend fun getAccessToken(userId: String) = tokenWebClient.post()
    .body(
      BodyInserters
        .fromFormData("grant_type", "client_credentials")
        .with("username", userId),
    )
    .retrieve()
    .bodyToMono<OAuthTokenResponse>()
    .timeout(1.seconds.toJavaDuration())
    .exponentialBackOffRetry()
    .awaitSingle()?.accessToken

  suspend fun getAuthorizedClient(userId: String): WebClient {
    val accessToken = getAccessToken(userId) ?: throw RuntimeException("Unexpected error obtaining token for user $userId")

    return WebClient
      .builder()
      .baseUrl(caseNotesRootUri)
      .defaultHeader("Authorization", "Bearer $accessToken")
      .defaultHeader("CaseloadId", "***")
      .build()
  }
}

data class OAuthTokenResponse(
  @JsonProperty("access_token")
  val accessToken: String,
)
