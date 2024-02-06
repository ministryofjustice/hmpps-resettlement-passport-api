package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class CaseNotesClientCredentialsService(val tokenWebClient: WebClient, @Value("\${api.base.url.case-notes}") private val caseNotesRootUri: String) {

  private fun getAccessToken(userId: String) = tokenWebClient.post()
    .body(
      BodyInserters
        .fromFormData("grant_type", "client_credentials")
        .with("username", userId),
    )
    .retrieve()
    .bodyToMono<OAuthTokenResponse>()
    .block()?.accessToken

  fun getAuthorizedClient(userId: String): WebClient {
    val accessToken = getAccessToken(userId) ?: throw RuntimeException("Unexpected error obtaining token for user $userId")
    return WebClient
      .builder()
      .baseUrl(caseNotesRootUri)
      .defaultHeader("Authorization", "Bearer $accessToken")
      .build()
  }
}

data class OAuthTokenResponse(
  @JsonProperty("access_token")
  val accessToken: String,
)
