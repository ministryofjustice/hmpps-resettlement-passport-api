package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.util.EnumSet
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private val RETRYABLE_STATUS = EnumSet.of(
  HttpStatus.INTERNAL_SERVER_ERROR,
  HttpStatus.BAD_GATEWAY,
  HttpStatus.SERVICE_UNAVAILABLE,
  HttpStatus.REQUEST_TIMEOUT,
)

private val exponentialBackOffRetry = Retry.backoff(2, 50.milliseconds.toJavaDuration())
  .maxBackoff(1.seconds.toJavaDuration())
  .filter { error: Throwable ->
    if (error is WebClientResponseException) {
      error.statusCode in RETRYABLE_STATUS
    } else {
      true
    }
  }
  .onRetryExhaustedThrow { _, rs -> rs.failure() }

fun <T> Mono<T>.exponentialBackOffRetry(): Mono<T> =
  this.retryWhen(exponentialBackOffRetry)
