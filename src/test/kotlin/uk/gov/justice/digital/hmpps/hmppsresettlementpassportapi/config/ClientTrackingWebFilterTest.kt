package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.springframework.http.HttpHeaders
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.JwtAuthHelper

class ClientTrackingWebFilterTest {
  private val clientTrackingWebFilter = ClientTrackingWebFilter()
  private val jwtAuthHelper = JwtAuthHelper()

  private val tracer: Tracer = otelTesting.openTelemetry.getTracer("test")
  private val filterChain = WebFilterChain { Mono.empty() }

  @Test
  fun shouldAddClientIdAndUserNameToInsightTelemetry() {
    // Given
    val token = jwtAuthHelper.createJwt("bob")
    val exchange = MockServerWebExchange.builder(
      MockServerHttpRequest.get("http://resettlementpassport")
        .header(HttpHeaders.AUTHORIZATION, "Bearer $token").build(),
    ).build()

    // When
    tracer.spanBuilder("span").startSpan().run {
      makeCurrent().use { clientTrackingWebFilter.filter(exchange, filterChain) }
      end()
    }

    // Then
    otelTesting.assertTraces().hasTracesSatisfyingExactly(
      { t ->
        t.hasSpansSatisfyingExactly(
          {
            it.hasAttribute(AttributeKey.stringKey("username"), "bob")
            it.hasAttribute(AttributeKey.stringKey("enduser.id"), "bob")
            it.hasAttribute(AttributeKey.stringKey("clientId"), "hmpps-resettlementpassport-api")
          },
        )
      },
    )
  }

  @Test
  fun shouldAddOnlyClientIdIfUsernameNullToInsightTelemetry() {
    // Given
    val token = jwtAuthHelper.createJwt(null)
    val exchange = MockServerWebExchange.builder(
      MockServerHttpRequest.get("http://resettlementpassport")
        .header(HttpHeaders.AUTHORIZATION, "Bearer $token").build(),
    ).build()

    // When
    tracer.spanBuilder("span").startSpan().run {
      makeCurrent().use { clientTrackingWebFilter.filter(exchange, filterChain) }
      end()
    }

    // Then
    otelTesting.assertTraces().hasTracesSatisfyingExactly(
      { t ->
        t.hasSpansSatisfyingExactly(
          {
            it.hasAttribute(AttributeKey.stringKey("clientId"), "hmpps-resettlementpassport-api")
          },
        )
      },
    )
  }

  private companion object {
    @JvmStatic
    @RegisterExtension
    private val otelTesting: OpenTelemetryExtension = OpenTelemetryExtension.create()
  }
}
