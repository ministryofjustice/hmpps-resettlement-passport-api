package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.events

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile

class DomainEventTest {
  private val objectMapper = Jackson2ObjectMapperBuilder.json().build<ObjectMapper>()

  @Test
  fun `Can get noms id`() {
    val event: DomainEvent = readEvent()

    assertThat(event.personReference.findNomsId()).isEqualTo("A4092EA")
  }

  private fun readEvent(): DomainEvent {
    val envelope: MessageEnvelope = objectMapper.readValue(readFile("testdata/events/recall-event.json"))
    val event: DomainEvent = objectMapper.readValue(envelope.message)
    return event
  }

  @Test
  fun `Can get prison id`() {
    val event: DomainEvent = readEvent()

    assertThat(event.prisonId()).isEqualTo("SWI")
  }

  @Test
  fun `Can get movement reason code`() {
    val event: DomainEvent = readEvent()

    assertThat(event.movementReasonCode()).isEqualTo("24")
  }
}
