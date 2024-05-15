package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

private val logInstance = LoggerFactory.getLogger(LoggingWebFilter::class.java)

@Component
class LoggingWebFilter(
  val log: Logger,
) : WebFilter {
  constructor() : this(logInstance)

  override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
    if (log.isDebugEnabled) {
      log.debug("[{}] Request received for path [{}]", exchange.request.id, exchange.request.uri)
    }
    val clientSessionId = exchange.request.headers.getFirst("SessionID")
    if (clientSessionId != null) {
      log.info("SessionID [{}]. [{}] Request received for path [{}]", clientSessionId, exchange.request.id, exchange.request.uri)
    }

    return chain.filter(exchange)
  }
}
