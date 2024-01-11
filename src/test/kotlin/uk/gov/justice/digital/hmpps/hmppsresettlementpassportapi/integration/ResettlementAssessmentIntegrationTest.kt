package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import java.time.LocalDateTime

class ResettlementAssessmentIntegrationTest : IntegrationTestBase() {

  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @Autowired
  private lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-2.sql")
  fun `Get Next assessment page happy path - Accommodation - New address to check answers`() {
    val expectedOutput = readFile("testdata/expectation/resettlement-assessment-1.json")
    val expectedOutput2 = readFile("testdata/expectation/resettlement-assessment-2.json")
    val nomsId = "G1458GV"
    var questions = mutableListOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
      ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE", answer = StringAnswer("NEW_ADDRESS")),
      ResettlementAssessmentRequestQuestionAndAnswer("WHAT_IS_THE_ADDRESS", answer = MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123")))),
    )
    val body = ResettlementAssessmentRequest(
      pathway = Pathway.ACCOMMODATION,
      nomsID = nomsId,
      type = ResettlementAssessmentType.BCST2,
      currentPage = "WHERE_WILL_THEY_LIVE",
      questions = questions,
      newStatus = Status.NOT_STARTED,
    )

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/next-page")
      .bodyValue(body)
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)

    var questions2 = mutableListOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
      ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE", answer = StringAnswer("NEW_ADDRESS")),
      ResettlementAssessmentRequestQuestionAndAnswer("WHAT_IS_THE_ADDRESS", answer = MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123")))),
      ResettlementAssessmentRequestQuestionAndAnswer(
        "WHO_WILL_THEY_LIVE_WITH",
        answer = MapAnswer(listOf(mapOf("name" to "person1", "age" to "47"), mapOf("name" to "person2", "age" to "53"))),
      ),
    )
    val body2 = ResettlementAssessmentRequest(
      pathway = Pathway.ACCOMMODATION,
      nomsID = nomsId,
      type = ResettlementAssessmentType.BCST2,
      currentPage = "WHO_WILL_THEY_LIVE_WITH",
      questions = questions2,
      newStatus = Status.NOT_STARTED,
    )
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/next-page")
      .bodyValue(body2)
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput2)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-1.sql")
  fun `Get resettlement assessment summary by noms ID - happy path`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val expectedOutput = readFile("testdata/expectation/resettlement-assessment-summary-1.json")

    val nomsId = "G4161UF"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/summary")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
     .exchange()
    .expectStatus().isOk
    .expectHeader().contentType("application/json")
    .expectBody()
    .json(expectedOutput)
   }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-2.sql")
  fun `Get resettlement assessment summary by noms ID- no assessments in DB - happy path`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow
    val expectedOutput = readFile("testdata/expectation/resettlement-assessment-summary-2.json")

    val nomsId = "G4161UF"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/summary")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  fun `Get resettlement assessment summary- unauthorized`() {
    val nomsId = "G4161UF"
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/summary")
      .exchange()
      .expectStatus().isEqualTo(401)
  }

  @Test
  fun `Get resettlement assessment summary-  forbidden`() {
    val nomsId = "G4161UF"
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/summary")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isEqualTo(403)
  }

  @Test
  fun `Get resettlement assessment summary- nomsId not found`() {
    val nomsId = "!--G4161UF"
    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/summary")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isEqualTo(404)
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
  }
}
