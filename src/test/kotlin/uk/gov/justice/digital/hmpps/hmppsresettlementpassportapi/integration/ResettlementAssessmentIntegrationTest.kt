package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Category
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ContactType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DeliusContactEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DpsCaseNoteEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentSimpleQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.StatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.DeliusContactRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.DpsCaseNoteRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import java.time.LocalDate
import java.time.LocalDateTime

class ResettlementAssessmentIntegrationTest : IntegrationTestBase() {

  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @Autowired
  private lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Autowired
  private lateinit var pathwayStatusRepository: PathwayStatusRepository

  @Autowired
  private lateinit var dpsCaseNoteRepository: DpsCaseNoteRepository

  @Autowired
  private lateinit var deliusContactRepository: DeliusContactRepository

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-2.sql")
  fun `Post get next assessment page happy path`() {
    val nomsId = "G1458GV"
    val pathway = "ACCOMMODATION"
    val assessmentType = "BCST2"
    val currentPage = "WHERE_DID_THEY_LIVE"
    val questions = mutableListOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
      ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE", answer = StringAnswer("PRIVATE_RENTED_HOUSING")),
      ResettlementAssessmentRequestQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS", answer = MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123")))),
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
          "nextPageId": "HELP_TO_KEEP_HOME"
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
    val page = "WHERE_DID_THEY_LIVE"

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
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-5.sql")
  fun `Get assessment check answers page happy path`() {
    val nomsId = "G1458GV"
    val pathway = "ACCOMMODATION"
    val assessmentType = "BCST2"
    val page = "CHECK_ANSWERS"

    val expectedOutput = readFile("testdata/expectation/resettlement-assessment-check-answers-1.json")

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
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), authSource = "nomis"))
      .exchange()
      .expectStatus().isOk

    val sampleAssessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf())

    val expectedResettlementAssessments = listOf(
      ResettlementAssessmentEntity(id = 2, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 1, name = "Accommodation", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:05:59.930557")), statusChangedTo = StatusEntity(id = 4, name = "Support Declined", active = true, creationDate = LocalDateTime.parse("2024-01-16T14:42:22.867169")), assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = sampleAssessment, creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatusEntity(id = 3, name = "Complete", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:06:00.219308")), caseNoteText = "Some case notes", createdByUserId = "JSMITH_GEN"),
      ResettlementAssessmentEntity(id = 1, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-17T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 1, name = "Accommodation", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:05:59.930557")), statusChangedTo = StatusEntity(id = 6, name = "Support Required", active = true, creationDate = LocalDateTime.parse("2024-01-16T14:42:22.867169")), assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "QUESTION_1", answer = ListAnswer(answer = listOf("Part 1", "Part 2", "Part 3"))), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "QUESTION_2", answer = MapAnswer(answer = listOf(mapOf("Key 1" to "Value 1", "Key 2" to "Value 2"), mapOf("Something" to "Something else")))), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_REQUIRED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "CASE_NOTE_SUMMARY", answer = StringAnswer(answer = "My case note summary...")))), creationDate = LocalDateTime.parse("2024-01-16T14:42:27.905483"), createdBy = "RESETTLEMENTPASSPORT_ADM", assessmentStatus = ResettlementAssessmentStatusEntity(id = 3, name = "Complete", active = true, creationDate = LocalDateTime.parse("2024-01-16T14:42:23.057474")), caseNoteText = "My case note summary...", createdByUserId = "RESETTLEMENTPASSPORT_ADM"),
    )
    val actualResettlementAssessments = resettlementAssessmentRepository.findAll()
    assertThat(actualResettlementAssessments).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(expectedResettlementAssessments)
  }

  @Test
  fun `Post resettlement assessment complete - incorrect auth_source`() {
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
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), authSource = "delius"))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType("application/json")
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
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), user = "System User", authSource = "nomis"))
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

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-4.sql")
  fun `Post resettlement assessment submit - happy path async`() {
    val nomsId = "ABC1234"
    val assessmentType = "BCST2"

    webTestClient.post()
      .uri("resettlement-passport/prisoner/$nomsId/resettlement-assessment/submit?assessmentType=$assessmentType")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), authSource = "nomis"))
      .exchange()
      .expectStatus().isOk

    // Check correct messages are on the case notes queue
    val caseNotesQueue = hmppsQueueService.findByQueueId("casenotes")
    val caseNotesQueueMessages = caseNotesQueue?.sqsClient?.receiveMessage { it.queueUrl(caseNotesQueue.queueUrl).maxNumberOfMessages(10) }?.get()
    Assertions.assertEquals(listOf("{\"caseNoteId\":1,\"userId\":\"USER_1\"}", "{\"caseNoteId\":2,\"userId\":\"USER_1\"}", "{\"caseNoteId\":3,\"userId\":\"USER_1\"}", "{\"caseNoteId\":4,\"userId\":\"USER_1\"}", "{\"caseNoteId\":5,\"userId\":\"USER_1\"}", "{\"caseNoteId\":6,\"userId\":\"USER_1\"}", "{\"caseNoteId\":7,\"userId\":\"USER_1\"}"), caseNotesQueueMessages?.messages()?.map { it.body() })

    // Check correct updates have been made to the database
    val assessmentsInDatabase = resettlementAssessmentRepository.findAll()
    val expectedAssessments = listOf(
      ResettlementAssessmentEntity(id = 1, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 1, name = "Accommodation", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.418488")), statusChangedTo = StatusEntity(id = 3, name = "Support Not Required", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.478585")), assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatusEntity(id = 4, name = "Submitted", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.696569")), caseNoteText = "Case note related to accommodation", createdByUserId = "USER_1"),
      ResettlementAssessmentEntity(id = 2, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 2, name = "Attitudes, thinking and behaviour", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.418488")), statusChangedTo = StatusEntity(id = 4, name = "Support Declined", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.478585")), assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatusEntity(id = 4, name = "Submitted", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.696569")), caseNoteText = "Case note related to Attitudes, thinking and behaviour", createdByUserId = "USER_1"),
      ResettlementAssessmentEntity(id = 3, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 3, name = "Children, families and communities", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.418488")), statusChangedTo = StatusEntity(id = 3, name = "Support Not Required", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.478585")), assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatusEntity(id = 4, name = "Submitted", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.696569")), caseNoteText = "Case note related to Children, family and communities", createdByUserId = "USER_1"),
      ResettlementAssessmentEntity(id = 4, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 4, name = "Drugs and alcohol", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.418488")), statusChangedTo = StatusEntity(id = 5, name = "Done", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.478585")), assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatusEntity(id = 4, name = "Submitted", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.696569")), caseNoteText = "Case note related to Drugs and alcohol", createdByUserId = "USER_1"),
      ResettlementAssessmentEntity(id = 5, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 5, name = "Education, skills and work", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.418488")), statusChangedTo = StatusEntity(id = 3, name = "Support Not Required", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.478585")), assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatusEntity(id = 4, name = "Submitted", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.696569")), caseNoteText = "Case note related to education, skills and work", createdByUserId = "USER_1"),
      ResettlementAssessmentEntity(id = 6, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 6, name = "Finance and ID", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.418488")), statusChangedTo = StatusEntity(id = 2, name = "In Progress", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.478585")), assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatusEntity(id = 4, name = "Submitted", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.696569")), caseNoteText = "Case note related to Finance and ID", createdByUserId = "USER_1"),
      ResettlementAssessmentEntity(id = 7, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 7, name = "Health", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.418488")), statusChangedTo = StatusEntity(id = 1, name = "Not Started", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.478585")), assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatusEntity(id = 4, name = "Submitted", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:25:24.696569")), caseNoteText = "Case note related to Health", createdByUserId = "USER_1"),
    )

    assertThat(assessmentsInDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(expectedAssessments)

    // Check pathway statuses have been updated
    val pathwayStatusesInDatabase = pathwayStatusRepository.findAll()
    val expectedPathwayStatuses = listOf(
      PathwayStatusEntity(id = 1, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 1, name = "Accommodation", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:48:33.399197")), status = StatusEntity(id = 3, name = "Support Not Required", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:48:33.469880")), updatedDate = LocalDateTime.parse("2024-01-31T14:48:42.924738")),
      PathwayStatusEntity(id = 2, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 2, name = "Attitudes, thinking and behaviour", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:48:33.399197")), status = StatusEntity(id = 4, name = "Support Declined", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:48:33.469880")), updatedDate = LocalDateTime.parse("2024-01-31T14:48:42.966093")),
      PathwayStatusEntity(id = 3, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 3, name = "Children, families and communities", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:48:33.399197")), status = StatusEntity(id = 3, name = "Support Not Required", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:48:33.469880")), updatedDate = LocalDateTime.parse("2024-01-31T14:48:42.984784")),
      PathwayStatusEntity(id = 4, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 4, name = "Drugs and alcohol", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:48:33.399197")), status = StatusEntity(id = 5, name = "Done", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:48:33.469880")), updatedDate = LocalDateTime.parse("2024-01-31T14:48:43.003119")),
      PathwayStatusEntity(id = 5, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 5, name = "Education, skills and work", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:48:33.399197")), status = StatusEntity(id = 3, name = "Support Not Required", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:48:33.469880")), updatedDate = LocalDateTime.parse("2024-01-31T14:48:43.023966")),
      PathwayStatusEntity(id = 6, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 6, name = "Finance and ID", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:48:33.399197")), status = StatusEntity(id = 2, name = "In Progress", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:48:33.469880")), updatedDate = LocalDateTime.parse("2024-01-31T14:48:43.042654")),
      PathwayStatusEntity(id = 7, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 7, name = "Health", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:48:33.399197")), status = StatusEntity(id = 1, name = "Not Started", active = true, creationDate = LocalDateTime.parse("2024-01-31T14:48:33.469880")), updatedDate = LocalDateTime.parse("2024-01-31T14:48:43.062076")),
    )

    assertThat(pathwayStatusesInDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(expectedPathwayStatuses)

    // Check delius_contacts have been added to the database
    val deliusContactsInDatabase = deliusContactRepository.findAll()
    val expectedDeliusContacts = listOf(
      DeliusContactEntity(id = 1, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), category = Category.ACCOMMODATION, contactType = ContactType.CASE_NOTE, createdDate = LocalDateTime.parse("2024-01-31T14:57:53.952046"), appointmentDate = null, appointmentDuration = null, notes = "Case note related to accommodation", createdBy = "A User"),
      DeliusContactEntity(id = 2, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), category = Category.ATTITUDES_THINKING_AND_BEHAVIOUR, contactType = ContactType.CASE_NOTE, createdDate = LocalDateTime.parse("2024-01-31T14:57:54.016061"), appointmentDate = null, appointmentDuration = null, notes = "Case note related to Attitudes, thinking and behaviour", createdBy = "A User"),
      DeliusContactEntity(id = 3, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), category = Category.CHILDREN_FAMILIES_AND_COMMUNITY, contactType = ContactType.CASE_NOTE, createdDate = LocalDateTime.parse("2024-01-31T14:57:54.042389"), appointmentDate = null, appointmentDuration = null, notes = "Case note related to Children, family and communities", createdBy = "A User"),
      DeliusContactEntity(id = 4, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), category = Category.DRUGS_AND_ALCOHOL, contactType = ContactType.CASE_NOTE, createdDate = LocalDateTime.parse("2024-01-31T14:57:54.059559"), appointmentDate = null, appointmentDuration = null, notes = "Case note related to Drugs and alcohol", createdBy = "A User"),
      DeliusContactEntity(id = 5, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), category = Category.EDUCATION_SKILLS_AND_WORK, contactType = ContactType.CASE_NOTE, createdDate = LocalDateTime.parse("2024-01-31T14:57:54.076275"), appointmentDate = null, appointmentDuration = null, notes = "Case note related to education, skills and work", createdBy = "A User"),
      DeliusContactEntity(id = 6, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), category = Category.FINANCE_AND_ID, contactType = ContactType.CASE_NOTE, createdDate = LocalDateTime.parse("2024-01-31T14:57:54.097218"), appointmentDate = null, appointmentDuration = null, notes = "Case note related to Finance and ID", createdBy = "A User"),
      DeliusContactEntity(id = 7, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), category = Category.HEALTH, contactType = ContactType.CASE_NOTE, createdDate = LocalDateTime.parse("2024-01-31T14:57:54.115010"), appointmentDate = null, appointmentDuration = null, notes = "Case note related to Health", createdBy = "A User"),
    )

    assertThat(deliusContactsInDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(expectedDeliusContacts)

    // Check dps_case_notes have been added to the database
    val dpsCaseNotesInDatabase = dpsCaseNoteRepository.findAll()
    val expectedDpsNotes = listOf(
      DpsCaseNoteEntity(id = 1, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 1, name = "Accommodation", active = true, creationDate = LocalDateTime.parse("2024-01-31T15:13:11.779810")), createdDate = LocalDateTime.parse("2024-01-31T15:13:22.102532"), notes = "Case note related to accommodation", createdBy = "A User"),
      DpsCaseNoteEntity(id = 2, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 2, name = "Attitudes, thinking and behaviour", active = true, creationDate = LocalDateTime.parse("2024-01-31T15:13:11.779810")), createdDate = LocalDateTime.parse("2024-01-31T15:13:22.150901"), notes = "Case note related to Attitudes, thinking and behaviour", createdBy = "A User"),
      DpsCaseNoteEntity(id = 3, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 3, name = "Children, families and communities", active = true, creationDate = LocalDateTime.parse("2024-01-31T15:13:11.779810")), createdDate = LocalDateTime.parse("2024-01-31T15:13:22.182949"), notes = "Case note related to Children, family and communities", createdBy = "A User"),
      DpsCaseNoteEntity(id = 4, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 4, name = "Drugs and alcohol", active = true, creationDate = LocalDateTime.parse("2024-01-31T15:13:11.779810")), createdDate = LocalDateTime.parse("2024-01-31T15:13:22.207773"), notes = "Case note related to Drugs and alcohol", createdBy = "A User"),
      DpsCaseNoteEntity(id = 5, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 5, name = "Education, skills and work", active = true, creationDate = LocalDateTime.parse("2024-01-31T15:13:11.779810")), createdDate = LocalDateTime.parse("2024-01-31T15:13:22.228386"), notes = "Case note related to education, skills and work", createdBy = "A User"),
      DpsCaseNoteEntity(id = 6, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 6, name = "Finance and ID", active = true, creationDate = LocalDateTime.parse("2024-01-31T15:13:11.779810")), createdDate = LocalDateTime.parse("2024-01-31T15:13:22.248694"), notes = "Case note related to Finance and ID", createdBy = "A User"),
      DpsCaseNoteEntity(id = 7, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 7, name = "Health", active = true, creationDate = LocalDateTime.parse("2024-01-31T15:13:11.779810")), createdDate = LocalDateTime.parse("2024-01-31T15:13:22.268440"), notes = "Case note related to Health", createdBy = "A User"),
    )

    assertThat(dpsCaseNotesInDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(expectedDpsNotes)
  }

  @Test
  fun `Post resettlement assessment submit - unauthorized`() {
    val nomsId = "ABC1234"
    val assessmentType = "BCST2"

    webTestClient.post()
      .uri("resettlement-passport/prisoner/$nomsId/resettlement-assessment/submit?assessmentType=$assessmentType")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Post resettlement assessment submit - forbidden`() {
    val nomsId = "ABC1234"
    val assessmentType = "BCST2"

    webTestClient.post()
      .uri("resettlement-passport/prisoner/$nomsId/resettlement-assessment/submit?assessmentType=$assessmentType")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType("application/json")
  }

  @Test
  fun `Post resettlement assessment submit - incorrect assessmentType`() {
    val nomsId = "ABC1234"
    val assessmentType = "notAnAssessmentType"

    webTestClient.post()
      .uri("resettlement-passport/prisoner/$nomsId/resettlement-assessment/submit?assessmentType=$assessmentType")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), authSource = "nomis"))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType("application/json")
  }

  @Test
  fun `Post resettlement assessment submit - incorrect auth_source`() {
    val nomsId = "ABC1234"
    val assessmentType = "BCST2"

    webTestClient.post()
      .uri("resettlement-passport/prisoner/$nomsId/resettlement-assessment/submit?assessmentType=$assessmentType")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), authSource = "delius"))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType("application/json")
  }

  @Test
  fun `Post resettlement assessment submit - no matching prisoner found`() {
    val nomsId = "ABC1234"
    val assessmentType = "BCST2"

    webTestClient.post()
      .uri("resettlement-passport/prisoner/$nomsId/resettlement-assessment/submit?assessmentType=$assessmentType")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), authSource = "nomis"))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
  }
}
