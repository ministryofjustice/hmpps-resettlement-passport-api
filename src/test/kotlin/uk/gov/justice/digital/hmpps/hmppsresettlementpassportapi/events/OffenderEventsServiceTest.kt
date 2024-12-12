package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.events

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.ResourceNotFoundException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.CaseAllocation
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.CaseAllocationService
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

  @Mock
  private lateinit var caseAllocationService: CaseAllocationService

  @BeforeEach
  fun beforeEach() {
    offenderEventsService = OffenderEventsService(offenderEventsRepository, pathwayAndStatusService, prisonerRepository, caseAllocationService)
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
    verifyCaseAllocationService()
    unmockkAll()
  }

  @Test
  fun `test handleReleaseEvent - Transfer Event`() {
    val randomUUID = UUID.randomUUID()
    mockkStatic(UUID::class)
    every { UUID.randomUUID() }.returns(randomUUID)

    val messageId = "123"
    val event = DomainEvent(
      eventType = "prison-offender-events.prisoner.released",
      occurredAt = ZonedDateTime.parse("2024-12-11T12:00:01+00:00"),
      additionalInformation = mapOf("reason" to "TRANSFERRED", "nomisMovementReasonCode" to "12"),
      personReference = PersonReference(listOf(PersonIdentifier(type = "NOMS", value = "abc2"))),
    )
    whenever(prisonerRepository.findByNomsId("abc2")).thenReturn(PrisonerEntity(id = 1, nomsId = "abc2", crn = null, prisonId = "ABC", creationDate = LocalDateTime.parse("2023-10-30T22:09:08")))

    offenderEventsService.handleReleaseEvent(messageId, event)

    Mockito.verifyNoMoreInteractions(prisonerRepository)
    Mockito.verifyNoMoreInteractions(offenderEventsRepository)
    verifyCaseAllocationService()
    unmockkAll()
  }

  @Test
  fun `test handleReleaseEvent - Transfer Event CaseAllocation Error`() {
    val randomUUID = UUID.randomUUID()
    mockkStatic(UUID::class)
    every { UUID.randomUUID() }.returns(randomUUID)

    val messageId = "123"
    val event = DomainEvent(
      eventType = "prison-offender-events.prisoner.released",
      occurredAt = ZonedDateTime.parse("2024-12-11T12:00:01+00:00"),
      additionalInformation = mapOf("reason" to "TRANSFERRED", "nomisMovementReasonCode" to "12"),
      personReference = PersonReference(listOf(PersonIdentifier(type = "NOMS", value = "abc2"))),
    )
    whenever(prisonerRepository.findByNomsId("abc2")).thenReturn(PrisonerEntity(id = 1, nomsId = "abc2", crn = null, prisonId = "ABC", creationDate = LocalDateTime.parse("2023-10-30T22:09:08")))
    whenever(caseAllocationService.unAssignCase(any())).thenThrow(ResourceNotFoundException("Unable to unassign, no officer assigned for prisoner with id abc2"))
    offenderEventsService.handleReleaseEvent(messageId, event)

    Mockito.verifyNoMoreInteractions(prisonerRepository)
    Mockito.verifyNoMoreInteractions(offenderEventsRepository)
    verifyCaseAllocationService()
    unmockkAll()
  }

  private fun verifyCaseAllocationService() {
    val captor = argumentCaptor<CaseAllocation>()
    Mockito.verify(caseAllocationService).unAssignCase(captor.capture())
    val capturedCaseAllocation = captor.firstValue
    assertArrayEquals(arrayOf("abc2"), capturedCaseAllocation.nomsIds)
  }
}
