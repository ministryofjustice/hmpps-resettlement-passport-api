package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.SYSTEM_USERNAME
import java.time.Duration

@Configuration
class WebClientConfiguration(
  @Value("\${api.base.url.oauth}") val authBaseUri: String,
  @Value("\${api.base.url.prison-register}") private val prisonRegisterRootUri: String,
  @Value("\${api.base.url.offender-search}") private val offenderSearchRootUri: String,
  @Value("\${api.base.url.cvl}") private val cvlRootUri: String,
  @Value("\${api.base.url.arn}") private val arnRootUri: String,
  @Value("\${api.base.url.prison}") private val prisonRootUri: String,
  @Value("\${api.base.url.offender-case-notes}") private val offenderCaseNotesRootUri: String,
  @Value("\${api.base.url.key-worker}") private val keyWorkerRootUri: String,
  @Value("\${api.base.url.allocation-manager}") private val allocationManagerRootUri: String,
  @Value("\${api.base.url.resettlement-passport-delius}") private val rpDeliusRootUri: String,
  @Value("\${api.base.url.education-employment}") private val educationEmploymentRootUri: String,
  @Value("\${api.base.url.ciag}") private val ciagRootUri: String,
  @Value("\${api.base.url.interventions-service}") private val interventionsRootUri: String,
) {

  @Bean
  fun prisonRegisterWebClientClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, prisonRegisterRootUri)
  }

  @Bean
  fun authorizedClientManager(
    clientRegistrationRepository: ReactiveClientRegistrationRepository,
    oAuth2AuthorizedClientService: ReactiveOAuth2AuthorizedClientService,
  ): ReactiveOAuth2AuthorizedClientManager {
    val authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
      .clientCredentials()
      .build()

    val authorizedClientManager = AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
      clientRegistrationRepository,
      oAuth2AuthorizedClientService,
    )
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
  }

  @Bean
  fun cvlWebClientClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, cvlRootUri)
  }

  @Bean
  fun offenderSearchWebClientClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, offenderSearchRootUri)
  }

  @Bean
  fun arnWebClientClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, arnRootUri)
  }

  @Bean
  fun prisonWebClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, prisonRootUri)
  }

  @Bean
  fun keyWorkerWebClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, keyWorkerRootUri)
  }

  @Bean
  fun offenderCaseNotesWebClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, offenderCaseNotesRootUri)
  }

  @Bean
  fun allocationManagerWebClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, allocationManagerRootUri)
  }

  @Bean
  fun rpDeliusWebClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, rpDeliusRootUri)
  }

  @Bean
  fun educationEmploymentWebClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, educationEmploymentRootUri)
  }

  @Bean
  fun ciagWebClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    return getWebClientCredentials(authorizedClientManager, ciagRootUri)
  }

  private fun getWebClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager, baseUrl: String): WebClient {
    val oauth2Client = ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(SYSTEM_USERNAME)

    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(baseUrl)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .filter(oauth2Client)
      .codecs { codecs -> codecs.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) }
      .build()
  }

  @Bean
  fun interventionsWebClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(SYSTEM_USERNAME)

    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(interventionsRootUri)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .filter(oauth2Client)
      .codecs { codecs -> codecs.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) }
      .build()
  }
}
