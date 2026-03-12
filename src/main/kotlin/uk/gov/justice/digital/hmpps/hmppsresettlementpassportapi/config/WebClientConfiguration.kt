package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import io.micrometer.observation.ObservationRegistry
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.http.codec.json.JacksonJsonEncoder
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.SYSTEM_USERNAME
import java.time.Duration
import java.util.function.Consumer

@Configuration
@EnableScheduling
class WebClientConfiguration(
  @param:Value("\${api.base.url.prisoner-search}") private val prisonerSearchRootUri: String,
  @param:Value("\${api.base.url.cvl}") private val cvlRootUri: String,
  @param:Value("\${api.base.url.arn}") private val arnRootUri: String,
  @param:Value("\${api.base.url.prison}") private val prisonRootUri: String,
  @param:Value("\${api.base.url.case-notes}") private val caseNotesRootUri: String,
  @param:Value("\${api.base.url.key-worker}") private val keyWorkerRootUri: String,
  @param:Value("\${api.base.url.allocation-manager}") private val allocationManagerRootUri: String,
  @param:Value("\${api.base.url.resettlement-passport-delius}") private val rpDeliusRootUri: String,
  @param:Value("\${api.base.url.education-employment}") private val educationEmploymentRootUri: String,
  @param:Value("\${api.base.url.interventions-service}") private val interventionsRootUri: String,
  @param:Value("\${api.base.url.pop-user-service}") private val popUserRootUri: String,
  @param:Value("\${api.base.url.gotenberg-api}") private val gotenbergRootUri: String,
  @param:Value("\${api.base.url.curious-service}") private val curiousRootUri: String,
  @param:Value("\${api.base.url.manage-users-service}") private val manageUsersRootUri: String,
  private val jsonMapper: JsonMapper,
  val clientRegistrationRepo: ClientRegistrationRepository,
  private val observationRegistry: ObservationRegistry,
) {

  @Bean
  fun authorizedClientManager(
    clientRegistrationRepository: ClientRegistrationRepository,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
  ): OAuth2AuthorizedClientManager {
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
      .clientCredentials()
      .build()

    val authorizedClientManager = AuthorizedClientServiceOAuth2AuthorizedClientManager(
      clientRegistrationRepository,
      oAuth2AuthorizedClientService,
    )
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
  }

  @Bean
  fun cvlWebClientClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient = getWebClientCredentials(authorizedClientManager, cvlRootUri)

  @Bean
  fun prisonerSearchWebClientClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient = getWebClientCredentials(authorizedClientManager, prisonerSearchRootUri)

  @Bean
  fun arnWebClientClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient = getWebClientCredentials(authorizedClientManager, arnRootUri)

  @Bean
  fun prisonWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient = getWebClientCredentials(authorizedClientManager, prisonRootUri)

  @Bean
  fun keyWorkerWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient = getWebClientCredentials(authorizedClientManager, keyWorkerRootUri)

  @Bean
  fun caseNotesWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient = getWebClientCredentials(authorizedClientManager, caseNotesRootUri, mapOf("CaseloadId" to "***"))

  @Bean
  fun allocationManagerWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient = getWebClientCredentials(authorizedClientManager, allocationManagerRootUri)

  @Bean
  fun rpDeliusWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient = getWebClientCredentials(authorizedClientManager, rpDeliusRootUri)

  @Bean
  fun educationEmploymentWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient = getWebClientCredentials(authorizedClientManager, educationEmploymentRootUri)

  @Bean
  fun interventionsWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient = getWebClientCredentials(authorizedClientManager, interventionsRootUri)

  private fun getWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager, baseUrl: String, defaultHeaders: Map<String, String> = emptyMap()): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(SYSTEM_USERNAME)

    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(baseUrl)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .filter(oauth2Client)
      .codecs(jsonCodecs())
      .defaultHeaders { headers ->
        defaultHeaders.forEach { (key, value) ->
          headers.set(key, value)
        }
      }
      .observationRegistry(observationRegistry)
      .build()
  }

  private fun jsonCodecs(): Consumer<ClientCodecConfigurer> = { codecs ->
    codecs.defaultCodecs().maxInMemorySize(5 * 1024 * 1024)
    codecs.defaultCodecs().jacksonJsonEncoder(JacksonJsonEncoder(jsonMapper))
  }

  @Bean
  fun tokenWebClient(): WebClient {
    val clientRegistration = clientRegistrationRepo.findByRegistrationId("RESETTLEMENT_PASSPORT_API")

    return WebClient.builder()
      .baseUrl(clientRegistration.providerDetails.tokenUri)
      .filter(ExchangeFilterFunctions.basicAuthentication(clientRegistration.clientId, clientRegistration.clientSecret))
      .observationRegistry(observationRegistry)
      .build()
  }

  @Bean
  fun popUserWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient = getWebClientCredentials(authorizedClientManager, popUserRootUri)

  @Bean
  fun gotenbergWebClient(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient = getWebClient(gotenbergRootUri)
  private fun getWebClient(baseUrl: String): WebClient {
    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(baseUrl)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .codecs(jsonCodecs())
      .observationRegistry(observationRegistry)
      .build()
  }

  @Bean
  fun curiousWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient = getWebClientCredentials(authorizedClientManager, curiousRootUri)

  @Bean
  fun manageUsersWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient = getWebClientCredentials(authorizedClientManager, manageUsersRootUri)
}
