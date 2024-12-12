package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.events

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.PathwayAndStatusService
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class OffenderEventsServiceTest {
  private lateinit var offenderEventsService: OffenderEventsService

  @Mock
  private lateinit var offenderEventsRepository: OffenderEventRepository

  @Mock
  private lateinit var pathwayAndStatusService: PathwayAndStatusService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @BeforeEach
  fun beforeEach() {
    offenderEventsService = OffenderEventsService(offenderEventsRepository, pathwayAndStatusService, prisonerRepository)
  }

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
    Mockito.verifyNoInteractions(offenderEventsRepository, prisonerRepository)
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
    Mockito.verifyNoInteractions(offenderEventsRepository, prisonerRepository)
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
  }

  @Test
  fun `test handleReleaseEvent - update prisoner's prisonId to OUT`() {
    val randomUUID = UUID.randomUUID()
    mockkStatic(UUID::class)
    every { UUID.randomUUID() }.returns(randomUUID)

    val messageId = "123"
    val event = DomainEvent(
      eventType = "prison-offender-events.prisoner.released",
      occurredAt = ZonedDateTime.parse("2024-12-11T12:00:01+00:00"),
      additionalInformation = mapOf("reason" to "RELEASED", "nomisMovementReasonCode" to "12"),
      personReference = PersonReference(listOf(PersonIdentifier(type = "NOMS", value = "abc2"))),
    )
    whenever(prisonerRepository.findByNomsId("abc2")).thenReturn(PrisonerEntity(id = 1, nomsId = "abc2", crn = null, prisonId = "ABC", creationDate = LocalDateTime.parse("2023-10-30T22:09:08")))

    offenderEventsService.handleReleaseEvent(messageId, event)

    Mockito.verify(prisonerRepository).save(PrisonerEntity(id = 1, nomsId = "abc2", crn = null, prisonId = "OUT", creationDate = LocalDateTime.parse("2023-10-30T22:09:08")))
    Mockito.verify(offenderEventsRepository).save(OffenderEventEntity(id = randomUUID, prisonerId = 1, type = OffenderEventType.PRISON_RELEASE, nomsId = "abc2", occurredAt = ZonedDateTime.parse("2024-12-11T12:00:01+00:00"), reasonCode = "12"))
    unmockkAll()
  }
}
