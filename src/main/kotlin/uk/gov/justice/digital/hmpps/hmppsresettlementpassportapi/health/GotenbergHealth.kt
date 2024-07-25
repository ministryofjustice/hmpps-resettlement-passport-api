package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.health
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component("gotenberg")
class GotenbergHealth(
  gotenbergWebClient: WebClient,
  @Value("gotenberg") componentName: String,
  @Value("\${api.base.url.gotenberg-api}") endpointUrl: String,
) : PingHealthCheck(gotenbergWebClient, componentName, "$endpointUrl/health")