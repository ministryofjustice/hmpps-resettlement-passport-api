package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.OneLoginUserData
import java.security.SecureRandom
import java.time.LocalDateTime

class PoPUserOTPIntegrationTest : IntegrationTestBase() {

  private val fakeNow = LocalDateTime.parse("2024-02-19T10:18:22.636066")

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp.sql")
  fun `Create, update and delete person on probation user otp - happy path`() {
    mockkStatic(LocalDateTime::class)
    mockkStatic(SecureRandom::class)
    every { LocalDateTime.now() } returns fakeNow
    every {
      SecureRandom.getInstanceStrong().nextLong(999999)
    } returns 123456

    val expectedOutput = readFile("testdata/expectation/pop-user-otp-post-result.json")
    val nomsId = "G4161UF"

    webTestClient.get()
      .uri("/resettlement-passport/popUser/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectBody().json("[]")

    webTestClient.get()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound

    webTestClient.post()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)

    webTestClient.get()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(expectedOutput)

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
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pop-user-otp.sql")
  fun `Get All OTP of Person on Probation Users - happy path`() {
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
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    mockkStatic(SecureRandom::class)
    every {
      SecureRandom.getInstanceStrong().nextLong(999999)
    } returns 123456

    val expectedOutput = readFile("testdata/expectation/pop-user-otp-post-result.json")
    val expectedOutput2 = readFile("testdata/expectation/pop-user-otp-post-result-2.json")
    val nomsId = "G4161UF"

    webTestClient.get()
      .uri("/resettlement-passport/popUser/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectBody().json("[]")

    webTestClient.get()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound

    webTestClient.post()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)

    every {
      SecureRandom.getInstanceStrong().nextLong(999999)
    } returns 567890

    webTestClient.post()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput2)

    webTestClient.get()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(expectedOutput2)
  }

  @Test
  fun `Get a Person on Probation all Users  - Unauthorized`() {
    webTestClient.get()
      .uri("/person-on-probation-user/popUser/otp")
      .exchange()
      .expectStatus().isUnauthorized
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
  fun `Get a Person on Probation all user OTP  - Forbidden`() {
    webTestClient.get()
      .uri("/resettlement-passport/popUser/otp")
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
    popUserApiMockServer.resetMappings()
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    mockkStatic(SecureRandom::class)
    every {
      SecureRandom.getInstanceStrong().nextLong(999999)
    } returns 123456

    val expectedOutput1 = readFile("testdata/expectation/pop-user-otp-post-result.json")
    val nomsId = "G4161UF"

    webTestClient.post()
      .uri("/resettlement-passport/popUser/$nomsId/otp")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput1)

    val expectedOutput2 = readFile("testdata/expectation/pop-user-verify-post-result.json")
    popUserApiMockServer.stubPostPoPUserVerification(200)
    webTestClient.post()
      .uri("/resettlement-passport/popUser/onelogin/verify")
      .bodyValue(
        OneLoginUserData(
          urn = "fdc:gov.uk:2022:T5fYp6sYl3DdYNF0tDfZtF-c4ZKewWRLw8YGcy6oEj8",
          otp = "123456",
          email = "chrisy.clemence@gmail.com",

        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput2)
  }
}
