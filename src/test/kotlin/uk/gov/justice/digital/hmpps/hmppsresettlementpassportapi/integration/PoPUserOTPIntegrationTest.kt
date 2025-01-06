package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PoPUserOTP
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PoPUserResponse
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class PoPUserOTPIntegrationTest : IntegrationTestBase() {
  @BeforeEach
  fun resetMappings() = popUserApiMockServer.resetMappings()

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

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp.sql")
  fun `Verify Person on Probation User OTP - happy path`() {
    val nomsId = "G4161UF"
    val urn = "fdc:gov.uk:2022:T5fYp6sYl3DdYNF0tDfZtF-c4ZKewWRLw8YGcy6oEj8"
    val dob = "1982-10-24"
    val email = "dave@dave.com"

    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    val createOtpResponse = webTestClient.post()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .returnBody<PoPUserOTP>()

    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, "abc")

    popUserApiMockServer.stubPostPoPUserVerification(200)
    verifyOtpResponse(createOtpResponse)

    val verifyResponse = webTestClient.post()
      .uri("/resettlement-passport/popUser/onelogin/verify")
      .bodyValue(
        mapOf(
          "urn" to urn,
          "otp" to createOtpResponse.otp,
          "dob" to dob,
          "email" to email,
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .returnBody<PoPUserResponse>()

    assertThat(verifyResponse.verified).isTrue
    assertThat(verifyResponse.id).isEqualTo(createOtpResponse.id)
    assertThat(verifyResponse.oneLoginUrn).isEqualTo(urn)
    assertThat(verifyResponse.nomsId).isEqualTo(nomsId)
    assertThat(verifyResponse.crn).isEqualTo("abc")
    assertThat(verifyResponse.cprId).isEqualTo("NA")
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp.sql")
  fun `Verify Person on Probation User OTP - Invalid OTP`() {
    val nomsId = "G4161UF"

    webTestClient.post()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .assertBody<PoPUserOTP>(::verifyOtpResponse)

    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    popUserApiMockServer.stubPostPoPUserVerification(200)
    webTestClient.post()
      .uri("/resettlement-passport/popUser/onelogin/verify")
      .bodyValue(
        mapOf(
          "urn" to "fdc:gov.uk:2022:T5fYp6sYl3DdYNF0tDfZtF-c4ZKewWRLw8YGcy6oEj8",
          "otp" to "123457",
          "dob" to "1982-10-24",
          "email" to "dave@dave.com",
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp-2.sql")
  fun `Verify Person on Probation User OTP - Expired OTP`() {
    val nomsId = "G4161UF"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    popUserApiMockServer.stubPostPoPUserVerification(200)

    webTestClient.post()
      .uri("/resettlement-passport/popUser/onelogin/verify")
      .bodyValue(
        mapOf(
          "urn" to "fdc:gov.uk:2022:T5fYp6sYl3DdYNF0tDfZtF-c4ZKewWRLw8YGcy6oEj8",
          "otp" to "1X3456",
          "dob" to "1982-10-24",
          "email" to "dave@dave.com",
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp-2.sql")
  fun `Verify Person on Probation User OTP - DOB no match`() {
    val nomsId = "G4161UF"

    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    popUserApiMockServer.stubPostPoPUserVerification(200)
    webTestClient.post()
      .uri("/resettlement-passport/popUser/onelogin/verify")
      .bodyValue(
        mapOf(
          "urn" to "fdc:gov.uk:2022:T5fYp6sYl3DdYNF0tDfZtF-c4ZKewWRLw8YGcy6oEj8",
          "otp" to "123456",
          "dob" to "2000-01-01",
          "email" to "dave@dave.com",
        ),

      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp.sql")
  fun `successfully verify probation user by knowledge questions`() {
    val nomsId = "G4161UF"
    val urn = "fdc:gov.uk:2022:T5fYp6sYl3DdYNF0tDfZtF-c4ZKewWRLw8YGcy6oEj8"
    val dob = "1982-10-24"
    val email = "john@smith.com"

    popUserApiMockServer.stubPostPoPUserVerification(200)
    prisonerSearchApiMockServer.stubMatchPrisonerOneMatch()

    webTestClient.post()
      .uri("/resettlement-passport/popUser/onelogin/verify-answers")
      .bodyValue(
        mapOf(
          "urn" to urn,
          "dateOfBirth" to dob,
          "email" to email,
          "nomsId" to nomsId,
          "firstName" to "John",
          "lastName" to "Smith",
        ),

      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .assertBody<PoPUserResponse> { response ->
        assertThat(response.verified).isTrue()
        assertThat(response.oneLoginUrn).isEqualTo(urn)
        assertThat(response.nomsId).isEqualTo(nomsId)
        assertThat(response.crn).isEqualTo("abc")
        assertThat(response.cprId).isEqualTo("NA")
        assertThat(response.id).isGreaterThanOrEqualTo(1)
      }

    val auditQueueMessage = sqsClient.receiveMessage(ReceiveMessageRequest.builder().queueUrl(auditQueueUrl).build()).get().messages()[0]
    assertThat(ObjectMapper().readValue(auditQueueMessage.body(), Map::class.java))
      .usingRecursiveComparison()
      .ignoringFields("when")
      .isEqualTo(mapOf("correlationId" to null, "details" to null, "service" to "hmpps-resettlement-passport-api", "subjectId" to "G4161UF", "subjectType" to "PRISONER_ID", "what" to "VERIFY_PYF_USER_BY_KNOWLEDGE_ANSWER", "when" to "2025-01-06T13:48:20.391273Z", "who" to "RESETTLEMENTPASSPORT_ADM"))
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp.sql")
  fun `reject verification of probation user by knowledge questions when dob is wrong`() {
    val nomsId = "G4161UF"
    val urn = "fdc:gov.uk:2022:T5fYp6sYl3DdYNF0tDfZtF-c4ZKewWRLw8YGcy6oEj8"
    val dob = "2010-01-01"
    val email = "john@smith.com"

    popUserApiMockServer.stubPostPoPUserVerification(200)
    prisonerSearchApiMockServer.stubMatchPrisonerOneMatch()

    webTestClient.post()
      .uri("/resettlement-passport/popUser/onelogin/verify-answers")
      .bodyValue(
        mapOf(
          "urn" to urn,
          "dateOfBirth" to dob,
          "email" to email,
          "nomsId" to nomsId,
          "firstName" to "John",
          "lastName" to "Smith",
        ),

      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp.sql")
  fun `reject verification of probation user by knowledge questions when nomis id is wrong`() {
    val urn = "fdc:gov.uk:2022:T5fYp6sYl3DdYNF0tDfZtF-c4ZKewWRLw8YGcy6oEj8"
    val dob = "2010-01-01"
    val email = "john@smith.com"

    popUserApiMockServer.stubPostPoPUserVerification(200)
    prisonerSearchApiMockServer.stubMatchPrisonerOneMatch()

    webTestClient.post()
      .uri("/resettlement-passport/popUser/onelogin/verify-answers")
      .bodyValue(
        mapOf(
          "urn" to urn,
          "dateOfBirth" to dob,
          "email" to email,
          "nomsId" to "WRONG",
          "firstName" to "John",
          "lastName" to "Smith",
        ),

      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp.sql")
  fun `reject verification of probation user by knowledge questions when no match found in search service`() {
    val nomsId = "G4161UF"
    val urn = "fdc:gov.uk:2022:T5fYp6sYl3DdYNF0tDfZtF-c4ZKewWRLw8YGcy6oEj8"
    val dob = "1982-10-24"
    val email = "john@smith.com"

    popUserApiMockServer.stubPostPoPUserVerification(200)
    prisonerSearchApiMockServer.stubMatchPrisonerNoMatch()

    webTestClient.post()
      .uri("/resettlement-passport/popUser/onelogin/verify-answers")
      .bodyValue(
        mapOf(
          "urn" to urn,
          "dateOfBirth" to dob,
          "email" to email,
          "nomsId" to nomsId,
          "firstName" to "John",
          "lastName" to "Smith",
        ),

      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp.sql")
  fun `reject verification of probation user by knowledge questions duplicate match found`() {
    val nomsId = "G4161UF"
    val urn = "fdc:gov.uk:2022:T5fYp6sYl3DdYNF0tDfZtF-c4ZKewWRLw8YGcy6oEj8"
    val dob = "1982-10-24"
    val email = "john@smith.com"

    popUserApiMockServer.stubPostPoPUserVerification(200)
    prisonerSearchApiMockServer.stubMatchPrisonerDuplicatedMatch()

    webTestClient.post()
      .uri("/resettlement-passport/popUser/onelogin/verify-answers")
      .bodyValue(
        mapOf(
          "urn" to urn,
          "dateOfBirth" to dob,
          "email" to email,
          "nomsId" to nomsId,
          "firstName" to "John",
          "lastName" to "Smith",
        ),

      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `unauthenticated verification`() {
    webTestClient.post()
      .uri("/resettlement-passport/popUser/onelogin/verify-answers")
      .bodyValue(
        mapOf(
          "urn" to "urn",
          "dateOfBirth" to "2021-01-01",
          "email" to "email@email.com",
          "nomsId" to "123",
          "firstName" to "John",
          "lastName" to "Smith",
        ),
      ).exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `unauthorized verification`() {
    webTestClient.post()
      .uri("/resettlement-passport/popUser/onelogin/verify-answers")
      .headers(setAuthorisation(roles = listOf("FISH")))
      .bodyValue(
        mapOf(
          "urn" to "urn",
          "dateOfBirth" to "2021-01-01",
          "email" to "email@email.com",
          "nomsId" to "123",
          "firstName" to "John",
          "lastName" to "Smith",
        ),
      ).exchange()
      .expectStatus().isForbidden
  }
}
