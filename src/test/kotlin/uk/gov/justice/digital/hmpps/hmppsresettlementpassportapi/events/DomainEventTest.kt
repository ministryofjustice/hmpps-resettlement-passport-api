package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.events

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.readFileAsObject
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.readStringAsObject

class DomainEventTest {
  @Test
  fun `Can get noms id`() {
    val event: DomainEvent = readEvent()

    assertThat(event.personReference.findNomsId()).isEqualTo("A4092EA")
  }

  private fun readEvent(): DomainEvent {
    val envelope: MessageEnvelope = readFileAsObject("testdata/events/recall-event.json")
    val event: DomainEvent = readStringAsObject(envelope.message)
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
