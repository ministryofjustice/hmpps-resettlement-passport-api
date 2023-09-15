package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.utils.UserContext

@Component
class UserContextWebFilter : WebFilter {
  override fun filter(serverWebExchange: ServerWebExchange, webFilterChain: WebFilterChain): Mono<Void> {
    val authToken = serverWebExchange.request.headers[HttpHeaders.AUTHORIZATION]?.firstOrNull() ?: "none"
    UserContext.authToken = authToken
    return webFilterChain.filter(serverWebExchange)
  }
}
