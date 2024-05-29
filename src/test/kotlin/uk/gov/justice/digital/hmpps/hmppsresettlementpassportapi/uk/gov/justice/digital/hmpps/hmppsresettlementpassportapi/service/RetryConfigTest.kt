package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.exponentialBackOffRetry
import java.net.URI

class RetryConfigTest {
  @Test
  fun `retries a network error`() {
    var calls = 0

    assertThatThrownBy {
      Mono.fromCallable { calls++ }
        .flatMap {
          Mono.error<String>(
            WebClientRequestException(
              Exception("some network error"),
              HttpMethod.POST,
              URI.create("http://url"),
              HttpHeaders.EMPTY,
            ),
          )
        }
        .exponentialBackOffRetry()
        .block()
    }.isInstanceOf(WebClientRequestException::class.java)

    assertThat(calls).isEqualTo(3)
  }

  @TestFactory
  fun `does not retry all status codes`() = listOf(400, 401, 403, 417).map { status ->
    dynamicTest("Should not retry a $status response") {
      var calls = 0

      assertThatThrownBy {
        Mono.fromCallable { calls++ }
          .flatMap {
            Mono.error<String>(
              WebClientResponseException.create(status, "$status", HttpHeaders.EMPTY, byteArrayOf(), Charsets.UTF_8),
            )
          }
          .exponentialBackOffRetry()
          .block()
      }.isInstanceOf(WebClientResponseException::class.java)

      assertThat(calls).isEqualTo(1)
    }
  }

  @TestFactory
  fun `should retry some status codes`() = listOf(500, 502, 503, 408).map { status ->
    dynamicTest("Should retry a $status response") {
      var calls = 0

      assertThatThrownBy {
        Mono.fromCallable { calls++ }
          .flatMap {
            Mono.error<String>(
              WebClientResponseException.create(status, "$status", HttpHeaders.EMPTY, byteArrayOf(), Charsets.UTF_8),
            )
          }
          .exponentialBackOffRetry()
          .block()
      }.isInstanceOf(WebClientResponseException::class.java)

      assertThat(calls).isEqualTo(3)
    }
  }
}
