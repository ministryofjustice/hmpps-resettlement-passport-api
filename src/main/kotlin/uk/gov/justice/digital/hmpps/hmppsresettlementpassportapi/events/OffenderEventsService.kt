package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.events

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository

private val logger = KotlinLogging.logger {}

private val RECALL_EVENT_CODES = setOf(
  "Y", // Recall from DTO
  "PD", // Post Recall Release
  "ELR", // Recall from End of Custody Licence (ECL)
  "B", // "Recall From HDC"
  "24", // Recall From Intermittent Custody
  "ETRLR", // Recall from Emergency Temporary Release
)

@Service
class OffenderEventsService(
  private val offenderEventRepository: OffenderEventRepository,
  private val prisonerRepository: PrisonerRepository,
) {

  fun handleReceiveEvent(messageId: String, event: DomainEvent) {
    logger.info { "Handling receive event from ${event.occurredAt}" }
    val nomsId = event.personReference.findNomsId() ?: run {
      logger.debug { "Ignoring $messageId as no nomsId" }
      return
    }
    val prisoner = prisonerRepository.findByNomsId(nomsId)
    if (prisoner == null) {
      logger.debug { "Ignoring $messageId as prisoner not found" }
      return
    }
    val reasonCode = event.movementReasonCode()

    offenderEventRepository.save(
      OffenderEventEntity(
        prisonerId = prisoner.id!!,
        type = OffenderEventType.PRISON_ADMISSION,
        nomsId = nomsId,
        occurredAt = event.occurredAt,
        reason = toReasonType(reasonCode),
        reasonCode = reasonCode,
      ),
    )
  }
}

internal fun toReasonType(movementReasonCode: String?): MovementReasonType? = when (movementReasonCode) {
  in RECALL_EVENT_CODES -> MovementReasonType.RECALL
  else -> null
}
