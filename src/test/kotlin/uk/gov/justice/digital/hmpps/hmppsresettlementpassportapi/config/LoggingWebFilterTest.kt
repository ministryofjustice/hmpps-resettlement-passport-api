package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@ExtendWith(MockitoExtension::class)
class LoggingWebFilterTest {

  @Mock
  private lateinit var webFilterChain: WebFilterChain

  @Mock
  private lateinit var mockLogger: Logger

  private lateinit var mockLoggerFactory: MockedStatic<LoggerFactory>

  @BeforeEach
  fun setup() {
    mockLoggerFactory = mockStatic(LoggerFactory::class.java)
    `when`(LoggerFactory.getLogger(any(Class::class.java))).thenReturn(mockLogger)
    `when`(LoggerFactory.getLogger(anyString())).thenReturn(mockLogger)
    `when`(mockLogger.name).thenReturn("mockLogger")
  }

  @AfterEach
  fun reset() {
    mockLoggerFactory.close()
  }

  @Test
  fun `Should filter without SessionID`() {
    val filter = LoggingWebFilter()
    val exchange = createMockExchange()
    `when`(webFilterChain.filter(exchange)).thenReturn(Mono.empty())
    filter.filter(exchange, webFilterChain).block()
    verify(webFilterChain).filter(exchange)
    verify(mockLogger, times(0)).info(any())
  }

  @Test
  fun `Should filter with SessionID`() {
    val filter = LoggingWebFilter()
    val exchange = createMockExchangeWithSessionID("testSessionID")
    `when`(webFilterChain.filter(exchange)).thenReturn(Mono.empty())

    filter.filter(exchange, webFilterChain).block()

    verify(webFilterChain).filter(exchange)
    verify(mockLogger).info(
      "SessionID [{}]. [{}] Request received for path [{}]",
      "testSessionID",
      exchange.request.id,
      exchange.request.uri,
    )
  }

  private fun createMockExchange(): MockServerWebExchange {
    val request = MockServerHttpRequest.get("/test").build()
    return MockServerWebExchange.from(request)
  }

  private fun createMockExchangeWithSessionID(sessionID: String): MockServerWebExchange {
    val request = MockServerHttpRequest.get("/test")
      .header(HttpHeaders.ACCEPT, "application/json")
      .header("SessionID", sessionID)
      .build()
    return MockServerWebExchange.from(request)
  }
}
