package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.mockk.unmockkAll
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentSimpleQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import java.time.LocalDate
import java.time.LocalDateTime

class LegacyResettlementAssessmentIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  // Tests using ATTITUDES_THINKING_AND_BEHAVIOUR Pathway - i.e. using old AbstractResettlementAssessmentStrategy
  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-2.sql")
  fun `Post get next assessment page happy path`() {
    val nomsId = "G1458GV"
    val pathway = "ATTITUDES_THINKING_AND_BEHAVIOUR"
    val assessmentType = "BCST2"
    val currentPage = "HELP_TO_MANAGE_ANGER"
    val questions = mutableListOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
      ResettlementAssessmentRequestQuestionAndAnswer("HELP_TO_MANAGE_ANGER", answer = StringAnswer("NO")),
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
          "nextPageId": "ISSUES_WITH_GAMBLING"
        }
        """.trimIndent(),
      )
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-2.sql")
  fun `Get assessment page happy path`() {
    val nomsId = "G1458GV"
    val pathway = "ATTITUDES_THINKING_AND_BEHAVIOUR"
    val assessmentType = "BCST2"
    val page = "HELP_TO_MANAGE_ANGER"

    val expectedOutput = readFile("testdata/expectation/resettlement-assessment-2.json")

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/page/$page?assessmentType=$assessmentType")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-9.sql")
  fun `Get assessment check answers page happy path`() {
    val nomsId = "G1458GV"
    val pathway = "ATTITUDES_THINKING_AND_BEHAVIOUR"
    val assessmentType = "BCST2"
    val page = "CHECK_ANSWERS"

    val expectedOutput = readFile("testdata/expectation/resettlement-assessment-check-answers-2.json")

    webTestClient.get()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/page/$page?assessmentType=$assessmentType")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-10.sql")
  fun `Post resettlement assessment complete - happy path`() {
    val nomsId = "ABC1234"
    val pathway = "ATTITUDES_THINKING_AND_BEHAVIOUR"
    val assessmentType = "BCST2"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/complete?assessmentType=$assessmentType")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "questionsAndAnswers": [
            {
              "question": "HELP_TO_MANAGE_ANGER",
              "answer": {
                "answer": "NO",
                "@class": "StringAnswer"
              }
            },
            {
              "question": "ISSUES_WITH_GAMBLING",
              "answer": {
                "answer": "YES",
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
      ResettlementAssessmentEntity(
        id = 2,
        prisoner = PrisonerEntity(
          id = 1,
          nomsId = "ABC1234",
          creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"),
          crn = "123",
          prisonId = "MDI",
          releaseDate = LocalDate.parse("2030-09-12"),
        ),
        pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR,
        statusChangedTo = Status.SUPPORT_DECLINED,
        assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN,
        assessment = sampleAssessment,
        creationDate = LocalDateTime.parse("2023-01-09T19:02:45"),
        createdBy = "A User",
        assessmentStatus = ResettlementAssessmentStatus.COMPLETE,
        caseNoteText = "Some case notes",
        createdByUserId = "JSMITH_GEN",
        submissionDate = null,
      ),
      ResettlementAssessmentEntity(
        id = 1,
        prisoner = PrisonerEntity(
          id = 1,
          nomsId = "ABC1234",
          creationDate = LocalDateTime.parse("2023-08-17T12:21:38.709"),
          crn = "123",
          prisonId = "MDI",
          releaseDate = LocalDate.parse("2030-09-12"),
        ),
        pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR,
        statusChangedTo = Status.SUPPORT_REQUIRED,
        assessmentType = ResettlementAssessmentType.BCST2,
        assessment = ResettlementAssessmentQuestionAndAnswerList(
          mutableListOf(
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "HELP_TO_MANAGE_ANGER",
              answer = StringAnswer(answer = "NO"),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "ISSUES_WITH_GAMBLING",
              answer = StringAnswer(answer = "YES"),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "SUPPORT_NEEDS",
              answer = StringAnswer(answer = "SUPPORT_REQUIRED"),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "CASE_NOTE_SUMMARY",
              answer = StringAnswer(answer = "My case note summary..."),
            ),
          ),
        ),
        creationDate = LocalDateTime.parse("2024-01-16T14:42:27.905483"),
        createdBy = "RESETTLEMENTPASSPORT_ADM",
        assessmentStatus = ResettlementAssessmentStatus.COMPLETE,
        caseNoteText = "My case note summary...",
        createdByUserId = "RESETTLEMENTPASSPORT_ADM",
        submissionDate = null,
      ),
    )
    val actualResettlementAssessments = resettlementAssessmentRepository.findAll()
    assertThat(actualResettlementAssessments).usingRecursiveComparison()
      .ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(expectedResettlementAssessments)
  }

  @Test
  fun `Post resettlement assessment complete - incorrect auth_source`() {
    val nomsId = "ABC1234"
    val pathway = "ATTITUDES_THINKING_AND_BEHAVIOUR"
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
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-3.sql")
  fun `Post resettlement assessment complete - nomsId not found in database`() {
    val nomsId = "DEF1234"
    val pathway = "ATTITUDES_THINKING_AND_BEHAVIOUR"
    val assessmentType = "BCST2"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/complete?assessmentType=$assessmentType")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "questionsAndAnswers": [
            {
              "question": "HELP_TO_MANAGE_ANGER",
              "answer": {
                "answer": "YES",
                "@class": "StringAnswer"
              }
            },
            {
              "question": "ISSUES_WITH_GAMBLING",
              "answer": {
                "answer": "NO",
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
      .headers(
        setAuthorisation(
          roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"),
          user = "System User",
          authSource = "nomis",
        ),
      )
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-3.sql")
  fun `Post resettlement assessment complete - invalid input json`() {
    val nomsId = "DEF1234"
    val pathway = "ATTITUDES_THINKING_AND_BEHAVIOUR"
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
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-5.sql")
  fun `Get latest resettlement assessment summary - happy path multiple assessments`() {
    val expectedOutput = readFile("testdata/expectation/latest-resettlement-assessment-4.json")

    val nomsId = "G4161UF"
    val pathway = "ATTITUDES_THINKING_AND_BEHAVIOUR"

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
    val expectedOutput = readFile("testdata/expectation/latest-resettlement-assessment-3.json")

    val nomsId = "G4161UF"
    val pathway = "ATTITUDES_THINKING_AND_BEHAVIOUR"

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
  fun `Uses pathway status for pre release assessment summary`() {
    val nomsId = "G4161UF"
    val pathway = "ATTITUDES_THINKING_AND_BEHAVIOUR"
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
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-11-bcst2-complete.sql")
  fun `Check your answers response for post submit of RESETTLEMENT_PLAN`() {
    val nomsId = "ABC1234"
    val pathway = "DRUGS_AND_ALCOHOL"
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
          "DRUG_ISSUES",
          "SUPPORT_WITH_DRUG_ISSUES",
          "ALCOHOL_ISSUES",
        ).doesNotContain("PRERELEASE_ASSESSMENT_SUMMARY")
      }
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-12-drugs-and-alcohol.sql")
  fun `Should not have case notes answer when getting the assessment summary page on a prepopulated RESETTLEMENT plan`() {
    val nomsId = "ABC1234"
    val pathway = "DRUGS_AND_ALCOHOL"
    val assessmentType = "RESETTLEMENT_PLAN"
    val page = "PRERELEASE_ASSESSMENT_SUMMARY"

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
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
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-12-drugs-and-alcohol.sql")
  fun `Should have case notes answer when getting the assessment summary page on BCST2`() {
    val nomsId = "ABC1234"
    val pathway = "DRUGS_AND_ALCOHOL"
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
      .isEqualTo("Carefully crafted case note answer")
  }
}
