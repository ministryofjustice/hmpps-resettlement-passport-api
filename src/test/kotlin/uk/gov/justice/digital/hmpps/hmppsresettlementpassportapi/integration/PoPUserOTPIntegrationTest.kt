package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PoPUserOTP
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class PoPUserOTPIntegrationTest : IntegrationTestBase() {
  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp.sql")
  fun `Create, update and delete person on probation user otp - happy path`() {
    val nomsId = "G4161UF"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)

    webTestClient.get()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound

    val postResponse: PoPUserOTP = webTestClient.post()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .returnBody<PoPUserOTP>()
    verifyOtpResponse(postResponse)

    webTestClient.get()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectBody<PoPUserOTP>(postResponse)

    webTestClient.delete()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk

    webTestClient.get()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound

    val auditQueueMessages = sqsClient.receiveMessage(ReceiveMessageRequest.builder().queueUrl(auditQueueUrl).maxNumberOfMessages(2).build()).get().messages()
    assertThat(ObjectMapper().readValue(auditQueueMessages[0].body(), Map::class.java))
      .usingRecursiveComparison()
      .ignoringFields("when")
      .isEqualTo(mapOf("correlationId" to null, "details" to null, "service" to "hmpps-resettlement-passport-api", "subjectId" to "G4161UF", "subjectType" to "PRISONER_ID", "what" to "CREATE_PYF_USER_OTP", "when" to "2025-01-06T13:48:20.391273Z", "who" to "RESETTLEMENTPASSPORT_ADM"))
    assertThat(ObjectMapper().readValue(auditQueueMessages[1].body(), Map::class.java))
      .usingRecursiveComparison()
      .ignoringFields("when")
      .isEqualTo(mapOf("correlationId" to null, "details" to null, "service" to "hmpps-resettlement-passport-api", "subjectId" to "G4161UF", "subjectType" to "PRISONER_ID", "what" to "DELETE_PYF_USER_OTP", "when" to "2025-01-06T13:48:20.391273Z", "who" to "RESETTLEMENTPASSPORT_ADM"))
  }

  private fun verifyOtpResponse(response: PoPUserOTP) {
    val now = LocalDateTime.now()
    val inSevenDaysAtMidnight = now.plusDays(7).withHour(23).withMinute(59).withSecond(59)
    assertThat(response.id).isGreaterThanOrEqualTo(1)
    assertThat(response.otp).hasSize(6)
    assertThat(response.creationDate).isCloseTo(now, within(10, ChronoUnit.SECONDS))
    assertThat(response.expiryDate).isCloseTo(inSevenDaysAtMidnight, within(10, ChronoUnit.SECONDS))
  }

  @Test
  fun `Get a Person on Probation User OTP by NomsId - Unauthorized`() {
    val nomsId = "G4161UF"

    webTestClient.get()
      .uri("/person-on-probation-user/popUser/$nomsId/otp")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp.sql")
  fun `Create Person on Probation User OTP another entry`() {
    val nomsId = "G4161UF"

    webTestClient.get()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound

    val firstOtp: PoPUserOTP = webTestClient.post()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .returnBody<PoPUserOTP>()
    verifyOtpResponse(firstOtp)

    val secondOtp: PoPUserOTP = webTestClient.post()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .returnBody<PoPUserOTP>()

    verifyOtpResponse(secondOtp)
    assertThat(secondOtp.id).isGreaterThan(firstOtp.id)
    assertThat(secondOtp.otp).isNotEqualTo(firstOtp.otp)

    webTestClient.get()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectBody<PoPUserOTP>(secondOtp)
  }

  @Test
  fun `Get a Person on Probation User OTP by nomsId - Forbidden`() {
    val nomsId = "G4161UF"

    webTestClient.get()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `Create  Person on Probation User OTP - Forbidden`() {
    val nomsId = "G4161UF"

    webTestClient.post()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }
}
