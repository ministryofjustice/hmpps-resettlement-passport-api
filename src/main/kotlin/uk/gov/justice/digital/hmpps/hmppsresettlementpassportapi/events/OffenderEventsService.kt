package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.events

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class OffenderEventsService {

  fun handleReceiveEvent(event: DomainEvent) {
    logger.info { "Handling receive event from ${event.occurredAt}" }
  }
}
