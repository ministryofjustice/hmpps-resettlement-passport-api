package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.observation.ObservationRegistry
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonEncoder
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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.SYSTEM_USERNAME
import java.time.Duration

@Configuration
@EnableScheduling
class WebClientConfiguration(
  @Value("\${api.base.url.prison-register}") private val prisonRegisterRootUri: String,
  @Value("\${api.base.url.prisoner-search}") private val prisonerSearchRootUri: String,
  @Value("\${api.base.url.cvl}") private val cvlRootUri: String,
  @Value("\${api.base.url.arn}") private val arnRootUri: String,
  @Value("\${api.base.url.prison}") private val prisonRootUri: String,
  @Value("\${api.base.url.case-notes}") private val caseNotesRootUri: String,
  @Value("\${api.base.url.key-worker}") private val keyWorkerRootUri: String,
  @Value("\${api.base.url.allocation-manager}") private val allocationManagerRootUri: String,
  @Value("\${api.base.url.resettlement-passport-delius}") private val rpDeliusRootUri: String,
  @Value("\${api.base.url.education-employment}") private val educationEmploymentRootUri: String,
  @Value("\${api.base.url.interventions-service}") private val interventionsRootUri: String,
  @Value("\${api.base.url.pop-user-service}") private val popUserRootUri: String,
  @Value("\${api.base.url.gotenberg-api}") private val gotenbergRootUri: String,
  @Value("\${api.base.url.curious-service}") private val curiousRootUri: String,
  private val objectMapper: ObjectMapper,
  val clientRegistrationRepo: ClientRegistrationRepository,
  private val observationRegistry: ObservationRegistry,

) {

  @Bean
  fun prisonRegisterWebClientClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager, observationRegistry: ObservationRegistry): WebClient {
    return getWebClientCredentials(authorizedClientManager, prisonRegisterRootUri, observationRegistry)
  }

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
  fun cvlWebClientClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, cvlRootUri, observationRegistry)
  }

  @Bean
  fun prisonerSearchWebClientClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, prisonerSearchRootUri, observationRegistry)
  }

  @Bean
  fun arnWebClientClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, arnRootUri, observationRegistry)
  }

  @Bean
  fun prisonWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, prisonRootUri, observationRegistry)
  }

  @Bean
  fun keyWorkerWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, keyWorkerRootUri, observationRegistry)
  }

  @Bean
  fun caseNotesWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, caseNotesRootUri, observationRegistry)
  }

  @Bean
  fun allocationManagerWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, allocationManagerRootUri, observationRegistry)
  }

  @Bean
  fun rpDeliusWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, rpDeliusRootUri, observationRegistry)
  }

  @Bean
  fun educationEmploymentWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, educationEmploymentRootUri, observationRegistry)
  }

  @Bean
  fun interventionsWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, interventionsRootUri, observationRegistry)
  }

  private fun getWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager, baseUrl: String, observationRegistry: ObservationRegistry): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(SYSTEM_USERNAME)

    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(baseUrl)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .filter(oauth2Client)
      .codecs { codecs ->
        codecs.defaultCodecs().maxInMemorySize(5 * 1024 * 1024)
        codecs.defaultCodecs().jackson2JsonEncoder(
          Jackson2JsonEncoder(
            objectMapper,
          ),
        )
      }
      .observationRegistry(observationRegistry)
      .build()
  }

  @Bean
  fun tokenWebClient(): WebClient {
    val clientRegistration = clientRegistrationRepo.findByRegistrationId("RESETTLEMENT_PASSPORT_API")

    return WebClient.builder()
      .baseUrl(clientRegistration.providerDetails.tokenUri)
      .filter(ExchangeFilterFunctions.basicAuthentication(clientRegistration.clientId, clientRegistration.clientSecret))
      .build()
  }

  @Bean
  fun popUserWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, popUserRootUri, observationRegistry)
  }

  @Bean
  fun gotenbergWebClient(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClient(gotenbergRootUri)
  }
  private fun getWebClient(baseUrl: String): WebClient {
    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(baseUrl)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .codecs { codecs ->
        codecs.defaultCodecs().maxInMemorySize(5 * 1024 * 1024)
        codecs.defaultCodecs().jackson2JsonEncoder(
          Jackson2JsonEncoder(
            objectMapper,
          ),
        )
      }
      .build()
  }

  @Bean
  fun curiousWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, curiousRootUri, observationRegistry)
  }
}
