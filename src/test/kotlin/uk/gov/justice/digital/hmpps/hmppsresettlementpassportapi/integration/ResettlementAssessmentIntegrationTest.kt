package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentSimpleQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.StatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import java.time.LocalDate
import java.time.LocalDateTime

class ResettlementAssessmentIntegrationTest : IntegrationTestBase() {

  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @Autowired
  private lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-2.sql")
  fun `Post get next assessment page happy path`() {
    val nomsId = "G1458GV"
    val pathway = "ACCOMMODATION"
    val assessmentType = "BCST2"
    val currentPage = "WHERE_WILL_THEY_LIVE"
    val questions = mutableListOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
      ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE", answer = StringAnswer("NEW_ADDRESS")),
      ResettlementAssessmentRequestQuestionAndAnswer("WHAT_IS_THE_ADDRESS", answer = MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123")))),
    )
    val body = ResettlementAssessmentRequest(
      questionsAndAnswers = questions,
    )

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/next-page?assessmentType=$assessmentType&currentPage=$currentPage")
      .bodyValue(body)
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(
        """
        {
          "nextPageId": "WHO_WILL_THEY_LIVE_WITH"
        }
        """.trimIndent(),
      )
  }

  @Test
  fun `Post get next assessment page unauthorized`() {
    val nomsId = "G1458GV"
    val pathway = "ACCOMMODATION"
    val assessmentType = "BCST2"
    val currentPage = "WHERE_WILL_THEY_LIVE"
    val questions = mutableListOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
      ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE", answer = StringAnswer("NEW_ADDRESS")),
      ResettlementAssessmentRequestQuestionAndAnswer("WHAT_IS_THE_ADDRESS", answer = MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123")))),
    )
    val body = ResettlementAssessmentRequest(
      questionsAndAnswers = questions,
    )

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/next-page?assessmentType=$assessmentType&currentPage=$currentPage")
      .bodyValue(body)
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Post get next assessment page forbidden`() {
    val nomsId = "G1458GV"
    val pathway = "ACCOMMODATION"
    val assessmentType = "BCST2"
    val currentPage = "WHERE_WILL_THEY_LIVE"
    val questions = mutableListOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
      ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE", answer = StringAnswer("NEW_ADDRESS")),
      ResettlementAssessmentRequestQuestionAndAnswer("WHAT_IS_THE_ADDRESS", answer = MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123")))),
    )
    val body = ResettlementAssessmentRequest(
      questionsAndAnswers = questions,
    )

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/next-page?assessmentType=$assessmentType&currentPage=$currentPage")
      .bodyValue(body)
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType("application/json")
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-2.sql")
  fun `Get assessment page happy path`() {
    val nomsId = "G1458GV"
    val pathway = "ACCOMMODATION"
    val assessmentType = "BCST2"
    val page = "WHERE_WILL_THEY_LIVE"

    val expectedOutput = readFile("testdata/expectation/resettlement-assessment-1.json")

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/page/$page?assessmentType=$assessmentType")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)
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
    unmockkAll()
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
    unmockkAll()
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

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-3.sql")
  fun `Post resettlement assessment complete - happy path`() {
    val nomsId = "ABC1234"
    val pathway = "ACCOMMODATION"
    val assessmentType = "BCST2"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/complete?assessmentType=$assessmentType")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "questionsAndAnswers": [
            {
              "question": "QUESTION_1",
              "answer": {
                "answer": ["Part 1", "Part 2", "Part 3"],
                "@class": "ListAnswer"
              }
            },
            {
              "question": "QUESTION_2",
              "answer": {
                "answer": [
                  {
                    "Key 1": "Value 1",
                    "Key 2": "Value 2"
                  },
                  {
                    "Something": "Something else"
                  }
                ],
                "@class": "MapAnswer"
              }
            },
            {
              "question": "SUPPORT_NEEDS",
              "answer": {
                "answer": "SUPPORT_REQUIRED",
                "@class": "StringAnswer"
              }
            },
            {
              "question": "CASE_NOTE_SUMMARY",
              "answer": {
                "answer": "My case note summary...",
                "@class": "StringAnswer"
              }
            }
          ]
        }
        """.trimIndent(),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk

    val sampleAssessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf())

    val expectedResettlementAssessments = listOf(
      ResettlementAssessmentEntity(id = 2, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 1, name = "Accommodation", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:05:59.930557")), statusChangedTo = null, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = sampleAssessment, creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatusEntity(id = 3, name = "Complete", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:06:00.219308")), caseNoteText = null),
      ResettlementAssessmentEntity(id = 1, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-17T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 1, name = "Accommodation", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:05:59.930557")), statusChangedTo = StatusEntity(id = 1, name = "Not Started", active = true, creationDate = LocalDateTime.parse("2024-01-16T14:42:22.867169")), assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "QUESTION_1", answer = ListAnswer(answer = listOf("Part 1", "Part 2", "Part 3"))), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "QUESTION_2", answer = MapAnswer(answer = listOf(mapOf("Key 1" to "Value 1", "Key 2" to "Value 2"), mapOf("Something" to "Something else")))), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_REQUIRED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "CASE_NOTE_SUMMARY", answer = StringAnswer(answer = "My case note summary...")))), creationDate = LocalDateTime.parse("2024-01-16T14:42:27.905483"), createdBy = "RESETTLEMENTPASSPORT_ADM", assessmentStatus = ResettlementAssessmentStatusEntity(id = 3, name = "Complete", active = true, creationDate = LocalDateTime.parse("2024-01-16T14:42:23.057474")), caseNoteText = "My case note summary..."),
    )
    val actualResettlementAssessments = resettlementAssessmentRepository.findAll()
    assertThat(actualResettlementAssessments).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(expectedResettlementAssessments)
  }

  @Test
  fun `Post resettlement assessment complete - unauthorized`() {
    val nomsId = "ABC1234"
    val pathway = "ACCOMMODATION"
    val assessmentType = "BCST2"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/complete?assessmentType=$assessmentType")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "questionsAndAnswers": []
        }
        """.trimIndent(),
      )
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Post resettlement assessment complete - forbidden`() {
    val nomsId = "ABC1234"
    val pathway = "ACCOMMODATION"
    val assessmentType = "BCST2"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/complete?assessmentType=$assessmentType")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "questionsAndAnswers": []
        }
        """.trimIndent(),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType("application/json")
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-3.sql")
  fun `Post resettlement assessment complete - nomsId not found in database`() {
    val nomsId = "DEF1234"
    val pathway = "ACCOMMODATION"
    val assessmentType = "BCST2"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/complete?assessmentType=$assessmentType")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "questionsAndAnswers": []
        }
        """.trimIndent(),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), user = "System User"))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-3.sql")
  fun `Post resettlement assessment complete - invalid input json`() {
    val nomsId = "DEF1234"
    val pathway = "ACCOMMODATION"
    val assessmentType = "BCST2"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/complete?assessmentType=$assessmentType")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "invalidField": "INFO"
        }
        """.trimIndent(),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), user = "System User"))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType("application/json")
  }
}