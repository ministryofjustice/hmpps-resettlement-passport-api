package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.events

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.PathwayAndStatusService

private val logger = KotlinLogging.logger {}

private val RECALL_EVENT_CODES = setOf(
  // Recall from DTO
  "Y",
  // Post Recall Release
  "PD",
  // Recall from End of Custody Licence (ECL)
  "ELR",
  // "Recall From HDC"
  "B",
  // Recall From Intermittent Custody
  "24",
  // Recall from Emergency Temporary Release
  "ETRLR",
  // Licence Revokee
  "L",
)

@Service
class OffenderEventsService(
  private val offenderEventRepository: OffenderEventRepository,
  private val pathwayAndStatusService: PathwayAndStatusService,
  private val prisonerRepository: PrisonerRepository,
) {

  @Transactional
  fun handleReceiveEvent(messageId: String, event: DomainEvent) {
    logger.info { "Handling receive event from ${event.occurredAt}" }
    val nomsId = event.personReference.findNomsId() ?: run {
      logger.debug { "Ignoring $messageId as no nomsId" }
      return
    }
    val prisonId = event.prisonId()
    val prisoner = pathwayAndStatusService.getOrCreatePrisoner(
      nomsId = nomsId,
      prisonId = prisonId,
      crn = event.personReference.findCrn(),
    )
    val reasonCode = event.movementReasonCode()

    if (prisoner.prisonId != prisonId) {
      prisonerRepository.save(prisoner.copy(prisonId = prisonId))
    }

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

  @Transactional
  fun handleReleaseEvent(messageId: String, event: DomainEvent) {
    logger.info { "Handling release event from ${event.occurredAt}" }
    val nomsId = event.personReference.findNomsId() ?: run {
      logger.info { "###### Ignoring release event $messageId as no nomsId" } // TODO put this back to debug
      return
    }

    if (event.reason() != "RELEASED") {
      logger.info { "###### Ignoring release event $messageId as reason is not RELEASED" }
      return
    }

    val prisoner = prisonerRepository.findByNomsId(nomsId)
    if (prisoner == null) {
      logger.info { "###### Ignoring release event $messageId as prisoner not found in database" }
      return
    }

    prisonerRepository.save(prisoner.copy(prisonId = "OUT"))

    offenderEventRepository.save(
      OffenderEventEntity(
        prisonerId = prisoner.id!!,
        type = OffenderEventType.PRISON_RELEASE,
        nomsId = nomsId,
        occurredAt = event.occurredAt,
        reason = null,
        reasonCode = event.movementReasonCode(),
      ),
    )
  }
}

internal fun toReasonType(movementReasonCode: String?): MovementReasonType? = when (movementReasonCode) {
  in RECALL_EVENT_CODES -> MovementReasonType.RECALL
  else -> null
}
