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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class OffenderEventsIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var hmppsQueueService: HmppsQueueService

  @Autowired
  private lateinit var repository: OffenderEventRepository

  @Autowired
  private lateinit var prisonerRepository: PrisonerRepository

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
      val saved = repository.findAllByPrisonerId(1)
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
      val saved = repository.findAllByPrisonerId(1)
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
      val saved = repository.findAllByPrisonerId(createdPrisoner?.id!!)
      assertThat(saved).hasSize(1)
      assertThat(saved[0].reason).isNull()
    }
  }
}
