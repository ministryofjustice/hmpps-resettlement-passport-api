package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.events

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.awspring.cloud.sqs.annotation.SqsListener
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

private val logger = KotlinLogging.logger {}

@Service
@ConditionalOnProperty("hmpps.sqs.queues.offender-events.queueName")
class OffenderEventsListener(
  private val objectMapper: ObjectMapper,
  private val offenderEventsService: OffenderEventsService,
) {

  @PostConstruct
  fun logStarted() {
    logger.info { "Started offender events listener" }
  }

  @SqsListener("offender-events", factory = "hmppsQueueContainerFactoryProxy")
  fun processMessage(envelope: MessageEnvelope) {
    val eventType = envelope.messageAttributes.eventType.value
    logger.debug { "Received message ${envelope.messageId} $eventType" }
    when (eventType) {
      "prison-offender-events.prisoner.received" -> {
        val event = objectMapper.readValue<DomainEvent>(envelope.message)
        offenderEventsService.handleReceiveEvent(envelope.messageId, event)
      }
      "prison-offender-events.prisoner.released" -> {
        val event = objectMapper.readValue<DomainEvent>(envelope.message)
        offenderEventsService.handleReleaseEvent(envelope.messageId, event)
      }

      else -> logger.debug { "Ignoring message ${envelope.messageId} with type $eventType" }
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

data class MessageEnvelope(
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
  fun movementReasonCode(): String? = additionalInformation["nomisMovementReasonCode"] as String?
  fun prisonId(): String? = additionalInformation["prisonId"] as String?
  fun reason(): String? = additionalInformation["reason"] as String?
}

data class PersonReference(val identifiers: List<PersonIdentifier> = listOf()) {
  fun findCrn() = get("CRN")
  fun findNomsId() = get("NOMS")
  operator fun get(key: String) = identifiers.find { it.type == key }?.value
}

data class PersonIdentifier(val type: String, val value: String)
