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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.CustomJwtAuthorisationHelper

class ClientTrackingWebFilterTest {
  private val clientTrackingWebFilter = ClientTrackingWebFilter()
  private val jwtAuthorisationHelper = CustomJwtAuthorisationHelper()

  private val tracer: Tracer = otelTesting.openTelemetry.getTracer("test")
  private val filterChain = WebFilterChain { Mono.empty() }

  @Test
  fun shouldAddClientIdAndUserNameToInsightTelemetry() {
    // Given
    val username = "bob"
    val exchange = MockServerWebExchange.builder(
      MockServerHttpRequest.get("http://resettlementpassport")
        .headers(headers(username = username)).build(),
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
            it.hasAttribute(AttributeKey.stringKey("username"), username)
            it.hasAttribute(AttributeKey.stringKey("enduser.id"), username)
            it.hasAttribute(AttributeKey.stringKey("clientId"), "hmpps-resettlementpassport-api")
          },
        )
      },
    )
  }

  @Test
  fun shouldAddOnlyClientIdIfUsernameNullToInsightTelemetry() {
    // Given
    val exchange = MockServerWebExchange.builder(
      MockServerHttpRequest.get("http://resettlementpassport")
        .headers(headers()).build(),
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

  private fun headers(username: String? = null) = HttpHeaders().apply {
    jwtAuthorisationHelper.setAuthorisationHeader(
      clientId = "hmpps-resettlementpassport-api",
      username = username,
    ).invoke(this)
  }
}
