package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.events

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.readFile
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException

class OffenderEventsIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var hmppsQueueService: HmppsQueueService

  @Autowired
  private lateinit var repository: OffenderEventRepository

  private val inboundQueue by lazy { hmppsQueueService.findByQueueId("inboundqueue") ?: throw MissingQueueException("HmppsQueue inboundqueue not found") }

  @Sql("classpath:testdata/sql/seed-prisoners-for-events.sql")
  @Test
  fun `Recall event is processed`() = runBlocking {
    inboundQueue.sqsClient.sendMessage { builder ->
      builder.queueUrl(inboundQueue.queueUrl)
        .messageBody(readFile("testdata/events/recall-event.json"))
    }.await()

    await.untilAsserted {
      val saved = repository.findAllByPrisonerId(1)
      assertThat(saved).hasSize(1)
      assertThat(saved[0].reason).isEqualTo(MovementReasonType.RECALL)
    }
  }

  @Sql("classpath:testdata/sql/seed-prisoners-for-events.sql")
  @Test
  fun `Admission event is processed`() = runBlocking {
    inboundQueue.sqsClient.sendMessage { builder ->
      builder.queueUrl(inboundQueue.queueUrl)
        .messageBody(readFile("testdata/events/intake-event.json"))
    }.await()

    await.untilAsserted {
      val saved = repository.findAllByPrisonerId(1)
      assertThat(saved).hasSize(1)
      assertThat(saved[0].reason).isNull()
    }
  }
}
