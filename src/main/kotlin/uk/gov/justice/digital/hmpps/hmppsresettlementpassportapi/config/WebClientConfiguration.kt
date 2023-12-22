package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.SYSTEM_USERNAME
import java.time.Duration

@Configuration
@EnableScheduling
class WebClientConfiguration(
  @Value("\${api.base.url.oauth}") val authBaseUri: String,
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
  @Value("\${api.base.url.ciag}") private val ciagRootUri: String,
  @Value("\${api.base.url.interventions-service}") private val interventionsRootUri: String,
) {

  @Bean
  fun prisonRegisterWebClientClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, prisonRegisterRootUri)
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
    return getWebClientCredentials(authorizedClientManager, cvlRootUri)
  }

  @Bean
  fun prisonerSearchWebClientClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, prisonerSearchRootUri)
  }

  @Bean
  fun arnWebClientClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, arnRootUri)
  }

  @Bean
  fun prisonWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, prisonRootUri)
  }

  @Bean
  fun keyWorkerWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, keyWorkerRootUri)
  }

  @Bean
  fun caseNotesWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, caseNotesRootUri)
  }

  @Bean
  fun caseNotesWebClientUserCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(SYSTEM_USERNAME)
    return WebClient.builder()
      .baseUrl(caseNotesRootUri)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .codecs { codecs -> codecs.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) }
      .build()
  }

  @Bean
  fun allocationManagerWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, allocationManagerRootUri)
  }

  @Bean
  fun rpDeliusWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, rpDeliusRootUri)
  }

  @Bean
  fun educationEmploymentWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, educationEmploymentRootUri)
  }

  @Bean
  fun ciagWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, ciagRootUri)
  }

  @Bean
  fun interventionsWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, interventionsRootUri)
  }

  private fun getWebClientCredentials(authorizedClientManager: OAuth2AuthorizedClientManager, baseUrl: String): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(SYSTEM_USERNAME)

    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(baseUrl)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .filter(oauth2Client)
      .codecs { codecs -> codecs.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) }
      .build()
  }
}
