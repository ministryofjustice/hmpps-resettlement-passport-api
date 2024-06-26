package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.events

import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.IntegrationTestBase
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import kotlin.time.Duration.Companion.seconds

class OffenderEventsIntegrationTest : IntegrationTestBase() {

  @Autowired
  protected lateinit var hmppsQueueService: HmppsQueueService

  private val inboundQueue by lazy { hmppsQueueService.findByQueueId("inboundqueue") ?: throw MissingQueueException("HmppsQueue inboundqueue not found") }

  @Test
  fun `event is processed`() = runBlocking {
    inboundQueue.sqsClient.sendMessage { builder ->
      builder.queueUrl(inboundQueue.queueUrl)
        .messageBody(
          """{
  "Type" : "Notification",
  "MessageId" : "b055dfa6-9777-584e-b11a-81551a8c480b",
  "TopicArn" : "arn:aws:sns:eu-west-2:754256621582:cloud-platform-Digital-Prison-Services-e29fb030a51b3576dd645aa5e460e573",
  "Message" : "{\"version\":1,\"eventType\":\"prison-offender-events.prisoner.received\",\"description\":\"A prisoner has been received into prison\",\"occurredAt\":\"2024-06-24T16:06:39+01:00\",\"publishedAt\":\"2024-06-24T16:07:39.665736228+01:00\",\"personReference\":{\"identifiers\":[{\"type\":\"NOMS\",\"value\":\"A4092EA\"}]},\"additionalInformation\":{\"nomsNumber\":\"A4092EA\",\"reason\":\"ADMISSION\",\"details\":\"ACTIVE IN:ADM-24\",\"currentLocation\":\"IN_PRISON\",\"prisonId\":\"SWI\",\"nomisMovementReasonCode\":\"24\",\"currentPrisonStatus\":\"UNDER_PRISON_CARE\"}}",
  "Timestamp" : "2024-06-24T15:07:39.672Z",
  "SignatureVersion" : "1",
  "Signature" : "XCg4kHPSWCG86DjeEcXb9UPIPRfOdYxXzVIgwfn4kGIJcZAg8Jn1Q4v9b3cYdBv630wnNVe89+v1S6QgocdweLU8QIZZKZ+8G0nqW71/3PKglAEbU6uAEaxpcamr4iQB1vVnBODUuiywwMuDuK+Ovl6c/HG8pc3l4Japr21tlCKmrv/UVRBFmSRn/LAIQGirGgcJ2+6Ea9kNxiqL+WsdclGpyFsZsf66qt0ktr91T+IsvQB0dgKhZsoq+Sg9LPSdQ2ab1vW9TE+HrdMdea38eMZgW1ANTpn4/jyWTTEo1NcdrRwW5mk3K33J1ECJb2hMoCAVzhyIBlmGQjE22SkrZQ==",
  "SigningCertURL" : "https://sns.eu-west-2.amazonaws.com/SimpleNotificationService-60eadc530605d63b8e62a523676ef735.pem",
  "UnsubscribeURL" : "https://sns.eu-west-2.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:eu-west-2:754256621582:cloud-platform-Digital-Prison-Services-e29fb030a51b3576dd645aa5e460e573:5dd178a1-9f0d-4590-b1d4-7fdd4e2a3082",
  "MessageAttributes" : {
    "eventType" : {"Type":"String","Value":"prison-offender-events.prisoner.received"}
  }
}""",
        )
    }.await()

    delay(2.seconds)
  }
}
