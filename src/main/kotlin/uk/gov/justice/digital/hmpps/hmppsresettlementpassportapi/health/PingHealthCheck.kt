package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.health

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.actuate.health.Status
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

abstract class PingHealthCheck(
  private val gotenbergWebClient: WebClient,
  private val componentName: String,
  private val healthUrl: String,
  private val timeout: Duration = Duration.ofSeconds(1),
  private val isOptionalComponent: Boolean = false,
) : HealthIndicator {

  private val optionalDownStatus: Status = Status("DOWN_BUT_OPTIONAL")

  @Autowired
  private val meterRegistry: MeterRegistry? = null

  private val gaugeVal: AtomicInteger = AtomicInteger(0)

  override fun health(): Health? {
    val result =
      try {
        gotenbergWebClient.get()
          .uri(healthUrl)
          .retrieve()
          .toEntity(String::class.java)
          .flatMap { upWithStatus(it) }
          .block(timeout)
      } catch (e: WebClientResponseException) {
        recordHealthMetricDown()
        downWithResponseBody(e)
      } catch (ex: Exception) {
        recordHealthMetricDown()
        downWithException(ex)
      }

    meterRegistry?.gauge("upstream_healthcheck", Tags.of("service", componentName), gaugeVal)

    if (result?.status == Status.UP) {
      recordHealthMetricUp()
    } else {
      recordHealthMetricDown()
    }

    return result
  }

  private fun recordHealthMetricUp() {
    gaugeVal.set(1)
  }

  private fun recordHealthMetricDown() {
    gaugeVal.set(0)
  }

  private fun downWithException(it: Exception): Health {
    val status = if (isOptionalComponent) optionalDownStatus else Status.DOWN
    return Health.status(status).withException(it).build()
  }

  private fun downWithResponseBody(it: WebClientResponseException): Health {
    val status = if (isOptionalComponent) optionalDownStatus else Status.DOWN
    return Health.status(status).withException(it).withBody(it.responseBodyAsString).withHttpStatus(it.statusCode)
      .build()
  }

  private fun upWithStatus(it: ResponseEntity<String>): Mono<Health> =
    Mono.just(Health.up().withHttpStatus(it.statusCode).build())

  private fun Health.Builder.withHttpStatus(status: HttpStatusCode) = this.withDetail("status", status)

  private fun Health.Builder.withBody(body: String) = this.withDetail("body", body)
}