package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.events

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.awspring.cloud.sqs.annotation.SqsListener
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

private val logger = KotlinLogging.logger {}

@Service
class OffenderEventsListener(
  private val objectMapper: ObjectMapper,
  private val offenderEventsService: OffenderEventsService,
) {

  @SqsListener("inboundqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun processMessage(message: Message) {
    logger.debug { "Received message ${message.messageId} ${message.message}" }
    when (val eventType = message.messageAttributes.eventType.value) {
      "prison-offender-events.prisoner.received" -> {
        val event = objectMapper.readValue<DomainEvent>(message.message)
        offenderEventsService.handleReceiveEvent(event)
      }

      else -> logger.debug { "Ignoring message ${message.messageId} with type $eventType" }
    }
  }
}

data class EventType(

  @JsonProperty("Value")
  val value: String,
  @JsonProperty("Type")
  val type: String,
)

data class MessageAttributes(val eventType: EventType)

data class Message(
  @JsonProperty("Message")
  val message: String,
  @JsonProperty("MessageId")
  val messageId: String,
  @JsonProperty("MessageAttributes")
  val messageAttributes: MessageAttributes,
)

data class DomainEvent(
  val eventType: String,
  val occurredAt: ZonedDateTime = ZonedDateTime.now(),
  @JsonSetter(nulls = Nulls.SKIP)
  val additionalInformation: Map<String, Any?> = emptyMap(),
  val personReference: PersonReference = PersonReference(),
) {
  fun movementReasonCode(): String? =
    additionalInformation["nomisMovementReasonCode"]?.takeIf { it is String } as String?
}

data class PersonReference(val identifiers: List<PersonIdentifier> = listOf()) {
  fun findCrn() = get("CRN")
  fun findNomsId() = get("NOMS")
  operator fun get(key: String) = identifiers.find { it.type == key }?.value
}

data class PersonIdentifier(val type: String, val value: String)
