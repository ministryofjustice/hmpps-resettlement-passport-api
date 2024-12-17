package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.events

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import kotlinx.coroutines.future.await
import kotlinx.coroutines.test.runTest
import org.awaitility.kotlin.await
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.CaseAllocationRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class OffenderEventsIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var hmppsQueueService: HmppsQueueService

  @Autowired
  private lateinit var offenderEventRepository: OffenderEventRepository

  @Autowired
  private lateinit var prisonerRepository: PrisonerRepository

  @Autowired
  private lateinit var caseAllocationRepository: CaseAllocationRepository

  private val offenderEventsQueue by lazy {
    hmppsQueueService.findByQueueId("offender-events") ?: throw MissingQueueException("Events queue not found")
  }

  @BeforeEach
  fun clearQueue() {
    offenderEventsQueue.sqsClient.purgeQueue { request -> request.queueUrl(offenderEventsQueue.queueUrl) }
  }

  @Sql("classpath:testdata/sql/seed-prisoners-for-events.sql")
  @Test
  fun `Recall event is processed`() = runTest {
    offenderEventsQueue.sqsClient.sendMessage { builder ->
      builder.queueUrl(offenderEventsQueue.queueUrl)
        .messageBody(readFile("testdata/events/recall-event.json"))
    }.await()

    await.atMost(2.seconds.toJavaDuration()).untilAsserted {
      val saved = offenderEventRepository.findAllByPrisonerId(1)
      assertThat(saved).hasSize(1)
      assertThat(saved[0].reason).isEqualTo(MovementReasonType.RECALL)
    }
    val prisoner = prisonerRepository.getReferenceById(1)
    assertThat(prisoner.prisonId).describedAs { "Prison id should be updated" }.isEqualTo("SWI")
  }

  @Sql("classpath:testdata/sql/seed-prisoners-for-events.sql")
  @Test
  fun `Admission event is processed`() = runTest {
    offenderEventsQueue.sqsClient.sendMessage { builder ->
      builder.queueUrl(offenderEventsQueue.queueUrl)
        .messageBody(readFile("testdata/events/intake-event.json"))
    }.await()

    await.atMost(2.seconds.toJavaDuration()).untilAsserted {
      val saved = offenderEventRepository.findAllByPrisonerId(1)
      assertThat(saved).hasSize(1)
      assertThat(saved[0].reason).isNull()
    }
  }

  @Test
  fun `Admission event is processed on new prisoner`() = runTest {
    offenderEventsQueue.sqsClient.sendMessage { builder ->
      builder.queueUrl(offenderEventsQueue.queueUrl)
        .messageBody(readFile("testdata/events/intake-event.json"))
    }.await()

    await.atMost(2.seconds.toJavaDuration()).untilAsserted {
      val createdPrisoner = prisonerRepository.findByNomsId("A4092EA")
      assertThat(createdPrisoner).isNotNull()
      val saved = offenderEventRepository.findAllByPrisonerId(createdPrisoner?.id!!)
      assertThat(saved).hasSize(1)
      assertThat(saved[0].reason).isNull()
    }
  }

  @Sql("classpath:testdata/sql/seed-prisoners-for-events.sql")
  @Test
  fun `Release event is processed`() = runTest {
    offenderEventsQueue.sqsClient.sendMessage { builder ->
      builder.queueUrl(offenderEventsQueue.queueUrl)
        .messageBody(readFile("testdata/events/release-event.json"))
    }.await()

    await.atMost(2.seconds.toJavaDuration()).untilAsserted {
      val savedOffenderEvent = offenderEventRepository.findAllByPrisonerId(1)
      assertThat(savedOffenderEvent).hasSize(1)
      assertThat(savedOffenderEvent[0]).usingRecursiveComparison().ignoringFields("id", "creationDate").isEqualTo(OffenderEventEntity(id = UUID.randomUUID(), prisonerId = 1, nomsId = "A4092EA", type = OffenderEventType.PRISON_RELEASE, occurredAt = ZonedDateTime.parse("2024-12-11T15:43:20+00:00"), reason = null, reasonCode = "NCS", creationDate = LocalDateTime.parse("2024-12-11T16:47:12.426329")))

      val savedPrisoner = prisonerRepository.findAll()
      assertThat(savedPrisoner).hasSize(1)
      assertThat(savedPrisoner[0]).isEqualTo(PrisonerEntity(1, "A4092EA", LocalDateTime.parse("2023-08-16T12:21:38.709"), "OUT"))
    }
  }

  @Sql("classpath:testdata/sql/seed-prisoners-for-events-unassign.sql")
  @Test
  fun `Release event is processed with unassign`() = runTest {
    offenderEventsQueue.sqsClient.sendMessage { builder ->
      builder.queueUrl(offenderEventsQueue.queueUrl)
        .messageBody(readFile("testdata/events/release-event.json"))
    }.await()

    await.atMost(2.seconds.toJavaDuration()).untilAsserted {
      val savedOffenderEvent = offenderEventRepository.findAllByPrisonerId(1)
      assertThat(savedOffenderEvent).hasSize(1)
      assertThat(savedOffenderEvent[0]).usingRecursiveComparison().ignoringFields("id", "creationDate").isEqualTo(OffenderEventEntity(id = UUID.randomUUID(), prisonerId = 1, nomsId = "A4092EA", type = OffenderEventType.PRISON_RELEASE, occurredAt = ZonedDateTime.parse("2024-12-11T15:43:20+00:00"), reason = null, reasonCode = "NCS", creationDate = LocalDateTime.parse("2024-12-11T16:47:12.426329")))

      val savedPrisoner = prisonerRepository.findAll()
      assertThat(savedPrisoner).hasSize(1)
      assertThat(savedPrisoner[0]).isEqualTo(PrisonerEntity(1, "A4092EA", LocalDateTime.parse("2023-08-16T12:21:38.709"), "OUT"))

      val savedCaseAllocation = caseAllocationRepository.findByPrisonerIdAndIsDeleted(1, true)
      assertThat(savedCaseAllocation).isNotNull
      assertThat(savedCaseAllocation?.id).isEqualTo(1)
      assertThat(savedCaseAllocation?.isDeleted).isEqualTo(true)
      assertThat(savedCaseAllocation?.deletionDate).isNotNull()
    }
  }
}
