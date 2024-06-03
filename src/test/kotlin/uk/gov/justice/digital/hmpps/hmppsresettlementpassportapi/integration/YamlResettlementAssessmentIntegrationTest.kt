package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.AssessmentSkipReason
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentSimpleQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.AssessmentSkipRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import java.time.LocalDate
import java.time.LocalDateTime

class YamlResettlementAssessmentIntegrationTest : IntegrationTestBase() {

  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @Autowired
  private lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Autowired
  private lateinit var pathwayStatusRepository: PathwayStatusRepository

  @Autowired
  private lateinit var assessmentSkipRepository: AssessmentSkipRepository

  // Tests using ACCOMMODATION Pathway - i.e. using new YamlResettlementAssessmentStrategy
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
          "nextPageId": "WHERE_DID_THEY_LIVE_ADDRESS"
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
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-6.sql")
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
    val assessmentType = "BCST2"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/summary?assessmentType=$assessmentType")
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
    val assessmentType = "BCST2"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/summary?assessmentType=$assessmentType")
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
    val assessmentType = "BCST2"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/summary?assessmentType=$assessmentType")
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
              "question": "WHERE_DID_THEY_LIVE",
              "answer": {
                "answer": "NO_PERMANENT_OR_FIXED",
                "@class": "StringAnswer"
              }
            },
            {
              "question": "WHERE_WILL_THEY_LIVE_2",
              "answer": {
                "answer": "DOES_NOT_HAVE_ANYWHERE",
                "@class": "StringAnswer"
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

    val sampleAssessment = ResettlementAssessmentQuestionAndAnswerList(assessment = mutableListOf())

    val expectedResettlementAssessments = listOf(
      ResettlementAssessmentEntity(id = 2, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_DECLINED, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = sampleAssessment, creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = "Some case notes", createdByUserId = "JSMITH_GEN", submissionDate = null),
      ResettlementAssessmentEntity(
        id = 1, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-17T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2,
        assessment = ResettlementAssessmentQuestionAndAnswerList(
          mutableListOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_2", answer = StringAnswer(answer = "DOES_NOT_HAVE_ANYWHERE")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_REQUIRED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "CASE_NOTE_SUMMARY", answer = StringAnswer(answer = "My case note summary..."))),
        ),
        creationDate = LocalDateTime.parse("2024-01-16T14:42:27.905483"), createdBy = "RESETTLEMENTPASSPORT_ADM", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = "My case note summary...", createdByUserId = "RESETTLEMENTPASSPORT_ADM", submissionDate = null,
      ),
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
          "questionsAndAnswers": [
            {
              "question": "WHERE_DID_THEY_LIVE",
              "answer": {
                "answer": "NO_PERMANENT_OR_FIXED",
                "@class": "StringAnswer"
              }
            },
            {
              "question": "WHERE_WILL_THEY_LIVE_2",
              "answer": {
                "answer": "DOES_NOT_HAVE_ANYWHERE",
                "@class": "StringAnswer"
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
  fun `Post resettlement assessment submit - happy path`() {
    val nomsId = "ABC1234"
    val assessmentType = "BCST2"

    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    caseNotesApiMockServer.stubPostCaseNotes(nomsId, "RESET", "BCST", "Case note summary from Accommodation Immediate needs report\\n\\nCase note related to accommodation", "MDI", 200)
    caseNotesApiMockServer.stubPostCaseNotes(nomsId, "RESET", "BCST", "Case note summary from Attitudes, thinking and behaviour Immediate needs report\\n\\nCase note related to Attitudes, thinking and behaviour", "MDI", 200)
    caseNotesApiMockServer.stubPostCaseNotes(nomsId, "RESET", "BCST", "Case note summary from Children, families and communities Immediate needs report\\n\\nCase note related to Children, family and communities", "MDI", 200)
    caseNotesApiMockServer.stubPostCaseNotes(nomsId, "RESET", "BCST", "Case note summary from Drugs and alcohol Immediate needs report\\n\\nCase note related to Drugs and alcohol", "MDI", 200)
    caseNotesApiMockServer.stubPostCaseNotes(nomsId, "RESET", "BCST", "Case note summary from Education, skills and work Immediate needs report\\n\\nCase note related to education, skills and work", "MDI", 200)
    caseNotesApiMockServer.stubPostCaseNotes(nomsId, "RESET", "BCST", "Case note summary from Finance and ID Immediate needs report\\n\\nCase note related to Finance and ID", "MDI", 200)
    caseNotesApiMockServer.stubPostCaseNotes(nomsId, "RESET", "BCST", "Case note summary from Health Immediate needs report\\n\\nCase note related to Health", "MDI", 200)

    webTestClient.post()
      .uri("resettlement-passport/prisoner/$nomsId/resettlement-assessment/submit?assessmentType=$assessmentType")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), authSource = "nomis"))
      .exchange()
      .expectStatus().isOk

    // Check correct updates have been made to the database
    val assessmentsInDatabase = resettlementAssessmentRepository.findAll()
    val expectedAssessments = listOf(
      ResettlementAssessmentEntity(id = 1, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_NOT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = mutableListOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = "Case note related to accommodation", createdByUserId = "USER_1", submissionDate = fakeNow),
      ResettlementAssessmentEntity(id = 2, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, statusChangedTo = Status.SUPPORT_DECLINED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = mutableListOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = "Case note related to Attitudes, thinking and behaviour", createdByUserId = "USER_1", submissionDate = fakeNow),
      ResettlementAssessmentEntity(id = 3, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, statusChangedTo = Status.SUPPORT_NOT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = mutableListOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = "Case note related to Children, family and communities", createdByUserId = "USER_1", submissionDate = fakeNow),
      ResettlementAssessmentEntity(id = 4, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = Pathway.DRUGS_AND_ALCOHOL, statusChangedTo = Status.DONE, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = mutableListOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = "Case note related to Drugs and alcohol", createdByUserId = "USER_1", submissionDate = fakeNow),
      ResettlementAssessmentEntity(id = 5, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = Pathway.EDUCATION_SKILLS_AND_WORK, statusChangedTo = Status.SUPPORT_NOT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = mutableListOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = "Case note related to education, skills and work", createdByUserId = "USER_1", submissionDate = fakeNow),
      ResettlementAssessmentEntity(id = 6, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = Pathway.FINANCE_AND_ID, statusChangedTo = Status.IN_PROGRESS, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = mutableListOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = "Case note related to Finance and ID", createdByUserId = "USER_1", submissionDate = fakeNow),
      ResettlementAssessmentEntity(id = 7, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = Pathway.HEALTH, statusChangedTo = Status.NOT_STARTED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = mutableListOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = "Case note related to Health", createdByUserId = "USER_1", submissionDate = fakeNow),
    )

    assertThat(assessmentsInDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(expectedAssessments)

    // Check pathway statuses have been updated
    val pathwayStatusesInDatabase = pathwayStatusRepository.findAll()
    val expectedPathwayStatuses = listOf(
      PathwayStatusEntity(id = 1, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = Pathway.ACCOMMODATION, status = Status.SUPPORT_NOT_REQUIRED, updatedDate = LocalDateTime.parse("2024-01-31T14:48:42.924738")),
      PathwayStatusEntity(id = 2, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, status = Status.SUPPORT_DECLINED, updatedDate = LocalDateTime.parse("2024-01-31T14:48:42.966093")),
      PathwayStatusEntity(id = 3, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, status = Status.SUPPORT_NOT_REQUIRED, updatedDate = LocalDateTime.parse("2024-01-31T14:48:42.984784")),
      PathwayStatusEntity(id = 4, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = Pathway.DRUGS_AND_ALCOHOL, status = Status.DONE, updatedDate = LocalDateTime.parse("2024-01-31T14:48:43.003119")),
      PathwayStatusEntity(id = 5, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = Pathway.EDUCATION_SKILLS_AND_WORK, status = Status.SUPPORT_NOT_REQUIRED, updatedDate = LocalDateTime.parse("2024-01-31T14:48:43.023966")),
      PathwayStatusEntity(id = 6, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = Pathway.FINANCE_AND_ID, status = Status.IN_PROGRESS, updatedDate = LocalDateTime.parse("2024-01-31T14:48:43.042654")),
      PathwayStatusEntity(id = 7, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = Pathway.HEALTH, status = Status.NOT_STARTED, updatedDate = LocalDateTime.parse("2024-01-31T14:48:43.062076")),
    )

    assertThat(pathwayStatusesInDatabase).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(expectedPathwayStatuses)
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

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-5.sql")
  fun `Get latest resettlement assessment summary - happy path multiple assessments`() {
    val expectedOutput = readFile("testdata/expectation/latest-resettlement-assessment-1.json")

    val nomsId = "G4161UF"
    val pathway = "ACCOMMODATION"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/latest")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput, true)
    unmockkAll()
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-7.sql")
  fun `Get latest resettlement assessment summary - happy path single assessment`() {
    val expectedOutput = readFile("testdata/expectation/latest-resettlement-assessment-2.json")

    val nomsId = "G4161UF"
    val pathway = "ACCOMMODATION"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/latest")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput, true)
    unmockkAll()
  }

  @Test
  fun `Get latest resettlement assessment summary - unauthorized`() {
    val nomsId = "G4161UF"
    val pathway = "ACCOMMODATION"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/latest")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Get latest resettlement assessment summary - forbidden`() {
    val nomsId = "G4161UF"
    val pathway = "ACCOMMODATION"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/latest")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType("application/json")
  }

  @Test
  fun `Get latest resettlement assessment summary - prisoner not found`() {
    val nomsId = "G4161UF"
    val pathway = "ACCOMMODATION"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/latest")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-2.sql")
  fun `Get latest resettlement assessment summary - no assessments found`() {
    val nomsId = "G4161UF"
    val pathway = "ACCOMMODATION"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/latest")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-7.sql")
  fun `Uses pathway status for pre release assessment summary`() {
    val nomsId = "G4161UF"
    val pathway = "ACCOMMODATION"
    val assessmentType = "RESETTLEMENT_PLAN"
    val page = "PRERELEASE_ASSESSMENT_SUMMARY"

    val expectedOutput = readFile("testdata/expectation/pre-release-assessment-summary.json")

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/page/$page?assessmentType=$assessmentType")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-2.sql")
  fun `Can skip BCST2 assessment when it is not started`() {
    val nomsId = "G4161UF"

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/skip?assessmentType=BCST2")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .header("Content-Type", "application/json")
      .bodyValue(
        """
          {
            "reason": "COMPLETED_IN_OASYS",
            "moreInfo": "something or other"
          }
        """,
      )
      .exchange()
      .expectStatus().isNoContent

    val savedSkip = assessmentSkipRepository.getReferenceById(1)

    assertThat(savedSkip.assessmentType).isEqualTo(ResettlementAssessmentType.BCST2)
    assertThat(savedSkip.prisonerId).isEqualTo(1)
    assertThat(savedSkip.reason).isEqualTo(AssessmentSkipReason.COMPLETED_IN_OASYS)
    assertThat(savedSkip.moreInfo).isEqualTo("something or other")
    assertThat(savedSkip.createdBy).isEqualTo("RESETTLEMENTPASSPORT_ADM")
    assertThat(savedSkip.creationDate).isNotNull()
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-7.sql")
  fun `Cannot skip BCST2 when it is started`() {
    val nomsId = "G4161UF"

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/skip?assessmentType=BCST2")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .header("Content-Type", "application/json")
      .bodyValue(
        """
          {
            "reason": "COMPLETED_IN_OASYS",
            "moreInfo": "something or other"
          }
        """,
      )
      .exchange()
      .expectStatus()
      .isBadRequest()
      .expectBody()
      .jsonPath("$.developerMessage")
      .value { message: String -> assertThat(message).contains("Cannot skip assessment that has already been started") }
  }

  @Test
  fun `Skip assessment requires edit role`() {
    val nomsId = "G4161UF"

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/skip?assessmentType=BCST2")
      .headers(setAuthorisation(roles = listOf("BAD_ROLE")))
      .header("Content-Type", "application/json")
      .bodyValue(
        """
          {
            "reason": "COMPLETED_IN_OASYS",
            "moreInfo": "something or other"
          }
        """,
      )
      .exchange()
      .expectStatus()
      .isForbidden()
  }

  @Test
  fun `Skip assessment requires auth`() {
    val nomsId = "G4161UF"

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/skip?assessmentType=BCST2")
      .header("Content-Type", "application/json")
      .bodyValue(
        """
          {
            "reason": "COMPLETED_IN_OASYS",
            "moreInfo": "something or other"
          }
        """,
      )
      .exchange()
      .expectStatus()
      .isUnauthorized()
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-11-bcst2-complete.sql")
  fun `Check your answers response for post submit of RESETTLEMENT_PLAN`() {
    val nomsId = "ABC1234"
    val pathway = "FINANCE_AND_ID"
    val assessmentType = "RESETTLEMENT_PLAN"
    val page = "CHECK_ANSWERS"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/page/$page?assessmentType=$assessmentType")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("questionsAndAnswers[*].originalPageId")
      .value { pageIds: List<String> ->
        assertThat(pageIds).containsExactly(
          "HAS_BANK_ACCOUNT",
          "HELP_WITH_BANK_ACCOUNT",
          "WHAT_ID_DOCUMENTS",
          "HELP_APPLY_FOR_ID",
          "RECEIVING_BENEFITS",
          "DEBTS_OR_ARREARS",
        ).doesNotContain("PRERELEASE_ASSESSMENT_SUMMARY")
      }
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-13-finance-and-id.sql")
  fun `Should not have case notes answer when getting the assessment summary page on a prepopulated RESETTLEMENT plan`() {
    val nomsId = "ABC1234"
    val pathway = "FINANCE_AND_ID"
    val assessmentType = "RESETTLEMENT_PLAN"
    val page = "PRERELEASE_ASSESSMENT_SUMMARY"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/page/$page?assessmentType=$assessmentType")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("questionsAndAnswers[?(@.question.id == 'CASE_NOTE_SUMMARY')].answer").isEqualTo(null)
      .jsonPath("questionsAndAnswers[?(@.question.id == 'SUPPORT_NEEDS_PRERELEASE')].answer.answer").isEqualTo("DONE")
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-13-finance-and-id.sql")
  fun `Should have case notes answer when getting the assessment summary page on BCST2`() {
    val nomsId = "ABC1234"
    val pathway = "FINANCE_AND_ID"
    val assessmentType = "BCST2"
    val page = "ASSESSMENT_SUMMARY"

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/page/$page?assessmentType=$assessmentType")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("questionsAndAnswers[?(@.question.id == 'CASE_NOTE_SUMMARY')].answer.answer")
      .isEqualTo("Expertly written case note answer")
  }
}
