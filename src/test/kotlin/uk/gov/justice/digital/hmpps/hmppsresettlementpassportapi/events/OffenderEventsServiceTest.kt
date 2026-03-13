package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.events

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.UUIDMockExtension
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.UUIDMockExtension.Companion.mockRandomUUID
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.CaseAllocationEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.CaseAllocationService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.PathwayAndStatusService
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

@ExtendWith(MockitoExtension::class, UUIDMockExtension::class)
class OffenderEventsServiceTest {
  private val offenderEventsService by lazy { OffenderEventsService(offenderEventsRepository, pathwayAndStatusService, prisonerRepository, caseAllocationService) }

  @Mock
  private lateinit var offenderEventsRepository: OffenderEventRepository

  @Mock
  private lateinit var pathwayAndStatusService: PathwayAndStatusService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var caseAllocationService: CaseAllocationService

  @Test
  fun `test handleReleaseEvent - no nomsId`() {
    val messageId = "123"
    val event = DomainEvent(
      eventType = "prison-offender-events.prisoner.released",
      occurredAt = ZonedDateTime.parse("2024-12-11T12:00:01+00:00"),
      additionalInformation = mapOf(),
      personReference = PersonReference(listOf(PersonIdentifier(type = "Some identifier", value = "abc1"))),
    )
    offenderEventsService.handleReleaseEvent(messageId, event)
    Mockito.verifyNoInteractions(offenderEventsRepository, prisonerRepository, caseAllocationService)
  }

  @Test
  fun `test handleReleaseEvent - reason not released`() {
    val messageId = "123"
    val event = DomainEvent(
      eventType = "prison-offender-events.prisoner.released",
      occurredAt = ZonedDateTime.parse("2024-12-11T12:00:01+00:00"),
      additionalInformation = mapOf("reason" to "OTHER"),
      personReference = PersonReference(listOf(PersonIdentifier(type = "NOMS", value = "abc2"))),
    )
    offenderEventsService.handleReleaseEvent(messageId, event)
    Mockito.verifyNoInteractions(offenderEventsRepository, prisonerRepository, caseAllocationService)
  }

  @Test
  fun `test handleReleaseEvent - prisoner not currently in database`() {
    val messageId = "123"
    val event = DomainEvent(
      eventType = "prison-offender-events.prisoner.released",
      occurredAt = ZonedDateTime.parse("2024-12-11T12:00:01+00:00"),
      additionalInformation = mapOf("reason" to "RELEASED"),
      personReference = PersonReference(listOf(PersonIdentifier(type = "NOMS", value = "abc2"))),
    )
    whenever(prisonerRepository.findByNomsId("abc2")).thenReturn(null)

    offenderEventsService.handleReleaseEvent(messageId, event)

    Mockito.verifyNoMoreInteractions(prisonerRepository)
    Mockito.verifyNoInteractions(offenderEventsRepository)
    Mockito.verifyNoInteractions(caseAllocationService)
  }

  @Test
  fun `test handleReleaseEvent - update prisoner's prisonId to OUT`() {
    val entityId = UUID.randomUUID()
      .also { mockRandomUUID(it) }

    val messageId = "123"
    val event = DomainEvent(
      eventType = "prison-offender-events.prisoner.released",
      occurredAt = ZonedDateTime.parse("2024-12-11T12:00:01+00:00"),
      additionalInformation = mapOf("reason" to "RELEASED", "nomisMovementReasonCode" to "12"),
      personReference = PersonReference(listOf(PersonIdentifier(type = "NOMS", value = "abc2"))),
    )
    whenever(prisonerRepository.findByNomsId("abc2")).thenReturn(PrisonerEntity(id = 1, nomsId = "abc2", prisonId = "ABC", creationDate = LocalDateTime.parse("2023-10-30T22:09:08")))

    offenderEventsService.handleReleaseEvent(messageId, event)

    Mockito.verify(prisonerRepository).save(PrisonerEntity(id = 1, nomsId = "abc2", prisonId = "OUT", creationDate = LocalDateTime.parse("2023-10-30T22:09:08")))
    Mockito.verify(offenderEventsRepository).save(OffenderEventEntity(id = entityId, prisonerId = 1, type = OffenderEventType.PRISON_RELEASE, nomsId = "abc2", occurredAt = ZonedDateTime.parse("2024-12-11T12:00:01+00:00"), reasonCode = "12"))
    Mockito.verify(caseAllocationService).getCaseAllocationByPrisonerId(1)
    Mockito.verifyNoMoreInteractions(caseAllocationService)
  }

  @Test
  fun `test handleReleaseEvent - Transfer Event`() {
    val messageId = "123"
    val event = DomainEvent(
      eventType = "prison-offender-events.prisoner.released",
      occurredAt = ZonedDateTime.parse("2024-12-11T12:00:01+00:00"),
      additionalInformation = mapOf("reason" to "TRANSFERRED", "nomisMovementReasonCode" to "12"),
      personReference = PersonReference(listOf(PersonIdentifier(type = "NOMS", value = "abc2"))),
    )
    whenever(prisonerRepository.findByNomsId("abc2")).thenReturn(PrisonerEntity(id = 1, nomsId = "abc2", prisonId = "ABC", creationDate = LocalDateTime.parse("2023-10-30T22:09:08")))

    offenderEventsService.handleReleaseEvent(messageId, event)

    Mockito.verifyNoMoreInteractions(prisonerRepository)
    Mockito.verifyNoMoreInteractions(offenderEventsRepository)
    Mockito.verify(caseAllocationService).getCaseAllocationByPrisonerId(1)
    Mockito.verifyNoMoreInteractions(caseAllocationService)
  }

  @Test
  fun `test handleReleaseEvent - Transfer Event CaseAllocation available`() {
    val messageId = "123"
    val event = DomainEvent(
      eventType = "prison-offender-events.prisoner.released",
      occurredAt = ZonedDateTime.parse("2024-12-11T12:00:01+00:00"),
      additionalInformation = mapOf("reason" to "TRANSFERRED", "nomisMovementReasonCode" to "12"),
      personReference = PersonReference(listOf(PersonIdentifier(type = "NOMS", value = "abc2"))),
    )

    val caseAllocationEntity = CaseAllocationEntity(1, 1, 123, "Joe", "Bloggs")

    whenever(prisonerRepository.findByNomsId("abc2")).thenReturn(PrisonerEntity(id = 1, nomsId = "abc2", prisonId = "ABC", creationDate = LocalDateTime.parse("2023-10-30T22:09:08")))
    whenever(caseAllocationService.getCaseAllocationByPrisonerId(1)).thenReturn(caseAllocationEntity)
    offenderEventsService.handleReleaseEvent(messageId, event)

    Mockito.verifyNoMoreInteractions(prisonerRepository)
    Mockito.verifyNoMoreInteractions(offenderEventsRepository)
    Mockito.verify(caseAllocationService).getCaseAllocationByPrisonerId(1)
    Mockito.verify(caseAllocationService).delete(caseAllocationEntity)
  }
}
