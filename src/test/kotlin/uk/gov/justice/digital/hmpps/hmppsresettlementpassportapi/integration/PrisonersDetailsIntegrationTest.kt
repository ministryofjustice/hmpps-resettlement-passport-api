package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration.wiremock.CvlApiMockServer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ProfileTagsRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.util.Base64

class PrisonersDetailsIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @Autowired
  lateinit var profileTagsRepository: ProfileTagsRepository

  @Test
  fun `Get Prisoner Details happy path - blank database - with caching`() {
    val expectedOutput = readFileAndReplaceAge("testdata/expectation/prisoner-details-2.json")
    val nomsId = "123"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    prisonApiMockServer.stubGetPrisonerImages(nomsId, 200)
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, "abc")
    deliusApiMockServer.stubGetPersonalDetailsFromCrn("abc", 200)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, true)

    val prisonerEntity1 = prisonerRepository.findByNomsId("123")
    val expectedPrisonerEntity1 = PrisonerEntity(null, nomsId, LocalDateTime.now(), "abc", "MDI", LocalDate.parse("2023-08-20"))
    assertThat(expectedPrisonerEntity1).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java)
      .ignoringFields("id").isEqualTo(prisonerEntity1)

    // Reset mocks to ensure it uses the cache
    prisonerSearchApiMockServer.resetAll()
    prisonApiMockServer.resetAll()
    deliusApiMockServer.resetAll()

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, true)

    val prisonerEntity2 = prisonerRepository.findByNomsId("123")
    val expectedPrisonerEntity2 = PrisonerEntity(null, nomsId, LocalDateTime.now(), "abc", "MDI", LocalDate.parse("2023-08-20"))
    assertThat(expectedPrisonerEntity2).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java)
      .ignoringFields("id").isEqualTo(prisonerEntity2)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Get Prisoner Details happy path - database seeded`() {
    val expectedOutput = readFileAndReplaceAge("testdata/expectation/prisoner-details-1.json")
    val nomsId = "123"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    prisonApiMockServer.stubGetPrisonerImages(nomsId, 200)
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, "abc")
    deliusApiMockServer.stubGetPersonalDetailsFromCrn("abc", 200)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, true)

    val prisonerEntity = prisonerRepository.findByNomsId("123")
    val expectedPrisonerEntity = PrisonerEntity(null, nomsId, LocalDateTime.now(), "abc", "xyz", LocalDate.parse("2025-01-23"))
    assertThat(expectedPrisonerEntity).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java)
      .ignoringFields("id").isEqualTo(prisonerEntity)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-12.sql")
  fun `Get Prisoner Details happy path - add in watchlist flag`() {
    var expectedOutput = readFile("testdata/expectation/prisoner-details-6.json")
    val dob = LocalDate.of(1982, 10, 24)
    val age = Period.between(dob, LocalDate.now()).years
    expectedOutput = expectedOutput.replace("REPLACE_WITH_AGE", "$age")
    val nomsId = "123"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    prisonApiMockServer.stubGetPrisonerImages(nomsId, 200)
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, "abc")
    deliusApiMockServer.stubGetPersonalDetailsFromCrn("abc", 200)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, true)

    val prisonerEntity = prisonerRepository.findByNomsId("123")
    val expectedPrisonerEntity = PrisonerEntity(null, nomsId, LocalDateTime.now(), "abc", "xyz", LocalDate.parse("2025-01-23"))
    assertThat(expectedPrisonerEntity).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java)
      .ignoringFields("id").isEqualTo(prisonerEntity)
  }

  @Test
  fun `Get Prisoner Details - error getting CRN - blank database`() {
    var expectedOutput = readFile("testdata/expectation/prisoner-details-5.json")
    val dob = LocalDate.of(1982, 10, 24)
    val age = Period.between(dob, LocalDate.now()).years
    expectedOutput = expectedOutput.replace("REPLACE_WITH_AGE", "$age")
    val nomsId = "123"
    val crn = "abc"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    prisonApiMockServer.stubGetPrisonerImages(nomsId, 200)
    deliusApiMockServer.stubGet("/probation-cases/$nomsId/crn", 404, null)
    deliusApiMockServer.stubGet("/probation-cases/$crn", 404, null)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, true)

    val prisonerEntity = prisonerRepository.findByNomsId("123")
    val expectedPrisonerEntity = PrisonerEntity(null, nomsId, LocalDateTime.now(), null, "MDI", LocalDate.parse("2023-08-20"))
    assertThat(expectedPrisonerEntity).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java)
      .ignoringFields("id").isEqualTo(prisonerEntity)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-3.sql")
  fun `Get Prisoner Details - add in CRN into seeded database`() {
    var expectedOutput = readFile("testdata/expectation/prisoner-details-1.json")
    val dob = LocalDate.of(1982, 10, 24)
    val age = Period.between(dob, LocalDate.now()).years
    expectedOutput = expectedOutput.replace("REPLACE_WITH_AGE", "$age")
    val nomsId = "123"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    prisonApiMockServer.stubGetPrisonerImages(nomsId, 200)
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, "abc")
    deliusApiMockServer.stubGetPersonalDetailsFromCrn("abc", 200)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, true)

    val prisonerEntity = prisonerRepository.findByNomsId("123")
    val expectedPrisonerEntity = PrisonerEntity(null, nomsId, LocalDateTime.now(), "abc", "xyz", LocalDate.parse("2030-09-12"))
    assertThat(expectedPrisonerEntity).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java)
      .ignoringFields("id").isEqualTo(prisonerEntity)
  }

  @Test
  fun `Get Prisoner Details unauthorized`() {
    val nomsId = "G4274GN"
    // Failing to set a valid Authorization header should result in 401 response
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId")
      .exchange()
      .expectStatus().isEqualTo(401)
  }

  @Test
  fun `Get Prisoner Details forbidden`() {
    val nomsId = "G4274GN"
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `Get Prisoner Details when nomsId not found`() {
    val nomsId = "abc"

    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 404)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
  }

  @Test
  fun `Get image for Prisoner happy path - with caching`() {
    val nomsId = "abc"
    val imageId = "1313058"
    val expectedOutput = Base64.getDecoder().decode(CvlApiMockServer.TEST_IMAGE_BASE64)

    prisonApiMockServer.stubGetPrisonerImages(nomsId, 200)
    prisonApiMockServer.stubGetPrisonerFacialImage(imageId, 200)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/image/$imageId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("image/jpeg")
      .expectBody<ByteArray>().consumeWith {
        Assertions.assertArrayEquals(expectedOutput, it.responseBody)
      }

    // Reset mocks to ensure it uses the cache
    prisonApiMockServer.resetAll()

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/image/$imageId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("image/jpeg")
      .expectBody<ByteArray>().consumeWith {
        Assertions.assertArrayEquals(expectedOutput, it.responseBody)
      }
  }

  @Test
  fun `Get Prisoner image not found`() {
    val nomsId = "abc"
    val imageId = "1313058"
    prisonApiMockServer.stubGetPrisonerImages(nomsId, 200)
    prisonApiMockServer.stubGetPrisonerFacialImage(imageId, 404)
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/image/$imageId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage").isEqualTo("Resource not found. Check request parameters - Image not found")
      .jsonPath("developerMessage").isEqualTo("Image not found")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  fun `Get Prisoner image unauthorized`() {
    val nomsId = "abc"
    val imageId = "1313058"
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/image/$imageId")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Get Prisoner image forbidden`() {
    val nomsId = "abc"
    val imageId = "1313058"
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/image/$imageId")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-8.sql")
  fun `Get Prisoner Details happy path - database seeded with assessments submitted`() {
    val expectedOutput = readFileAndReplaceAge("testdata/expectation/prisoner-details-3.json")
    val nomsId = "123"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    prisonApiMockServer.stubGetPrisonerImages(nomsId, 200)
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, "abc")
    deliusApiMockServer.stubGetPersonalDetailsFromCrn("abc", 200)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, true)

    val prisonerEntity = prisonerRepository.findByNomsId("123")
    val expectedPrisonerEntity = PrisonerEntity(null, nomsId, LocalDateTime.now(), "abc", "xyz", LocalDate.parse("2025-01-23"))
    assertThat(expectedPrisonerEntity).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java)
      .ignoringFields("id").isEqualTo(prisonerEntity)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-10.sql")
  fun `Get Prisoner Details happy path - database seeded with assessments and resettlement review submitted`() {
    val expectedOutput = readFileAndReplaceAge("testdata/expectation/prisoner-details-4.json")
    val nomsId = "123"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    prisonApiMockServer.stubGetPrisonerImages(nomsId, 200)
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, "abc")
    deliusApiMockServer.stubGetPersonalDetailsFromCrn("abc", 200)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, true)

    val prisonerEntity = prisonerRepository.findByNomsId("123")
    val expectedPrisonerEntity = PrisonerEntity(null, nomsId, LocalDateTime.now(), "abc", "xyz", LocalDate.parse("2025-01-23"))
    assertThat(expectedPrisonerEntity).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java)
      .ignoringFields("id").isEqualTo(prisonerEntity)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-11.sql")
  fun `Get Prisoner Details - resettlement plan in progress, with no BCST2`() {
    val nomsId = "123"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    prisonApiMockServer.stubGetPrisonerImages(nomsId, 200)
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, "abc")
    deliusApiMockServer.stubGetPersonalDetailsFromCrn("abc", 200)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("$.assessmentRequired").isEqualTo(false)
      .jsonPath("$.resettlementReviewAvailable").isEqualTo(true)
      .jsonPath("$.immediateNeedsSubmitted").isEqualTo(false)
      .jsonPath("$.preReleaseSubmitted").isEqualTo(false)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-13.sql")
  fun `Get Prisoner Details with profile tags happy path - database seeded`() {
    val expectedOutput = readFileAndReplaceAge("testdata/expectation/prisoner-details-7.json")
    val nomsId = "123"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    prisonApiMockServer.stubGetPrisonerImages(nomsId, 200)
    deliusApiMockServer.stubGetCrnFromNomsId(nomsId, "abc")
    deliusApiMockServer.stubGetPersonalDetailsFromCrn("abc", 200)

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId?includeProfileTags=true")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput, true)
  }

  private fun readFileAndReplaceAge(resourceName: String): String {
    val prisonerJson = readFile(resourceName)
    val dob = LocalDate.of(1982, 10, 24)
    val age = Period.between(dob, LocalDate.now()).years
    return prisonerJson.replace("REPLACE_WITH_AGE", "$age")
  }
}
