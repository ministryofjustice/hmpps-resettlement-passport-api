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
  @Value("\${api.base.url.prison-register}") private val prisonRootUri: String,
  @Value("\${api.base.url.offender-search}") private val offenderSearchUri: String,
  @Value("\${api.base.url.cvl}") private val cvlRootUri: String,
  @Value("\${api.base.url.arn}") private val arnRootUri: String,
  @Value("\${api.base.url.prison}") private val prisonerImageUri: String,
  @Value("\${api.base.url.offender-case-notes}") private val offenderCaseNotesUri: String,
  @Value("\${api.base.url.key-worker}") private val keyWorkerRootUri: String,
  @Value("\${api.base.url.allocation-manager}") private val allocationManagerRootUri: String,
  @Value("\${api.base.url.resettlement-passport-delius}") private val rpDeliusRootUri: String,
) {

  @Bean
  fun authWebClient(): WebClient {
    return WebClient.builder()
      .baseUrl(authBaseUri)
      .build()
  }

  @Bean
  fun prisonWebClient(): WebClient {
    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(prisonRootUri)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .filter(AuthTokenFilterFunction())
      .build()
  }

  @Bean
  fun prisonWebClientClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(SYSTEM_USERNAME)

    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(prisonRootUri)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .filter(oauth2Client)
      .build()
  }

  @Bean
  fun prisonHealthWebClient(): WebClient {
    return WebClient.builder()
      .baseUrl(prisonRootUri)
      .build()
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
    val oauth2Client = ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(SYSTEM_USERNAME)

    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(cvlRootUri)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .filter(oauth2Client)
      .codecs { codecs -> codecs.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) }
      .build()
  }

  @Bean
  fun offendersSearchWebClientClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(SYSTEM_USERNAME)

    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(offenderSearchUri)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .filter(oauth2Client)
      .codecs { codecs -> codecs.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) }
      .build()
  }

  @Bean
  fun arnWebClientClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(SYSTEM_USERNAME)

    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(arnRootUri)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .filter(oauth2Client)
      .codecs { codecs -> codecs.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) }
      .build()
  }

  @Bean
  fun offendersImageWebClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(SYSTEM_USERNAME)

    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(prisonerImageUri)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .filter(oauth2Client)
      .codecs { codecs -> codecs.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) }
      .build()
  }

  @Bean
  fun offenderCaseNotesWebClientUserCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    val oauth2Client = ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(SYSTEM_USERNAME)
    return WebClient.builder()
      .baseUrl(offenderCaseNotesUri)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .codecs { codecs -> codecs.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) }
      .build()
  }

  @Bean
  fun keyWorkerWebClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(SYSTEM_USERNAME)

    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(keyWorkerRootUri)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .filter(oauth2Client)
      .codecs { codecs -> codecs.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) }
      .build()
  }

  @Bean
  fun offenderCaseNotesWebClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(SYSTEM_USERNAME)

    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(offenderCaseNotesUri)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .filter(oauth2Client)
      .codecs { codecs -> codecs.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) }
      .build()
  }

  @Bean
  fun allocationManagerWebClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(SYSTEM_USERNAME)

    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(allocationManagerRootUri)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .filter(oauth2Client)
      .codecs { codecs -> codecs.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) }
      .build()
  }

  @Bean
  fun rpDeliusWebClientCredentials(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(SYSTEM_USERNAME)

    val httpClient = HttpClient.create().responseTimeout(Duration.ofMinutes(2))
    return WebClient.builder()
      .baseUrl(rpDeliusRootUri)
      .clientConnector(ReactorClientHttpConnector(httpClient))
      .filter(oauth2Client)
      .codecs { codecs -> codecs.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) }
      .build()
  }
}
