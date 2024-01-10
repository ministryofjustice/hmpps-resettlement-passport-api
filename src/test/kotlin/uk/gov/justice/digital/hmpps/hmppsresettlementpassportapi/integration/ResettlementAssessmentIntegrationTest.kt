package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.StatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import java.time.LocalDate
import java.time.LocalDateTime

class ResettlementAssessmentIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-2.sql")
  fun `Get Next assessment page happy path - Accommodation - New address to check answers`() {
    val expectedOutput = readFile("testdata/expectation/resettlement-assessment-1.json")
    val expectedOutput2 = readFile("testdata/expectation/resettlement-assessment-2.json")
    val nomsId = "G1458GV"
    val questions = mutableListOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
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

    val questions2 = mutableListOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
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
  @Sql("classpath:testdata/sql/seed-resettlement-assessments-1.sql")
  fun `Post resettlement assessment submit - happy path`() {
    val nomsId = "ABC1234"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/submit")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "pathway": "ACCOMMODATION",
          "assessmentType": "RESETTLEMENT_PLAN",
          "supportNeed": "SUPPORT_REQUIRED",
          "caseNoteSummary": "Testing 123"
        }
        """.trimIndent(),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk

    val expectedResettlementAssessments = listOf(
      ResettlementAssessmentEntity(id = 1, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 1, name = "Accommodation", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:05:59.930557")), statusChangedTo = null, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = "{}", creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatusEntity(id = 1, name = "Not Started", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:06:00.219308")), caseNoteText = null),
      ResettlementAssessmentEntity(id = 3, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 1, name = "Accommodation", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:05:59.930557")), statusChangedTo = null, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = "{}", creationDate = LocalDateTime.parse("2023-01-28T09:56:42"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatusEntity(id = 1, name = "Not Started", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:06:00.219308")), caseNoteText = null),
      ResettlementAssessmentEntity(id = 4, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 2, name = "Attitudes, thinking and behaviour", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:05:59.930557")), statusChangedTo = null, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = "{}", creationDate = LocalDateTime.parse("2023-01-10T23:34"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatusEntity(id = 2, name = "In Progress", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:06:00.219308")), caseNoteText = null),
      ResettlementAssessmentEntity(id = 5, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 3, name = "Children, families and communities", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:05:59.930557")), statusChangedTo = null, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = "{}", creationDate = LocalDateTime.parse("2023-01-11T21:12:23"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatusEntity(id = 2, name = "In Progress", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:06:00.219308")), caseNoteText = null),
      ResettlementAssessmentEntity(id = 6, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 4, name = "Drugs and alcohol", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:05:59.930557")), statusChangedTo = null, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = "{}", creationDate = LocalDateTime.parse("2023-01-13T16:09:01"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatusEntity(id = 1, name = "Not Started", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:06:00.219308")), caseNoteText = null),
      ResettlementAssessmentEntity(id = 7, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 5, name = "Education, skills and work", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:05:59.930557")), statusChangedTo = null, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = "{}", creationDate = LocalDateTime.parse("2023-01-12T04:32:12"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatusEntity(id = 3, name = "Complete", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:06:00.219308")), caseNoteText = null),
      ResettlementAssessmentEntity(id = 8, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 7, name = "Health", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:05:59.930557")), statusChangedTo = null, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = "{}", creationDate = LocalDateTime.parse("2023-01-17T16:43:49"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatusEntity(id = 3, name = "Complete", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:06:00.219308")), caseNoteText = null),
      ResettlementAssessmentEntity(id = 2, prisoner = PrisonerEntity(id = 1, nomsId = "ABC1234", creationDate = LocalDateTime.parse("2023-08-16T12:21:38.709"), crn = "123", prisonId = "MDI", releaseDate = LocalDate.parse("2030-09-12")), pathway = PathwayEntity(id = 1, name = "Accommodation", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:05:59.930557")), statusChangedTo = StatusEntity(id = 1, name = "Not Started", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:05:59.999407")), assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = "{}", creationDate = LocalDateTime.parse("2023-02-09T10:01:23"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatusEntity(id = 3, name = "Complete", active = true, creationDate = LocalDateTime.parse("2024-01-09T14:06:00.219308")), caseNoteText = "Testing 123"),
    )
    val actualResettlementAssessments = resettlementAssessmentRepository.findAll()
    assertThat(actualResettlementAssessments).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(expectedResettlementAssessments)
  }

  @Test
  fun `Post resettlement assessment submit - unauthorized`() {
    val nomsId = "ABC1234"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/submit")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "pathway": "ACCOMMODATION",
          "assessmentType": "RESETTLEMENT_PLAN",
          "supportNeed": "SUPPORT_REQUIRED",
          "caseNoteSummary": "Testing 123"
        }
        """.trimIndent(),
      )
      .exchange()
      .expectStatus().isUnauthorized
      .expectHeader().contentType("application/json")
  }

  @Test
  fun `Post resettlement assessment submit - forbidden`() {
    val nomsId = "ABC1234"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/submit")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "pathway": "ACCOMMODATION",
          "assessmentType": "RESETTLEMENT_PLAN",
          "supportNeed": "SUPPORT_REQUIRED",
          "caseNoteSummary": "Testing 123"
        }
        """.trimIndent(),
      )
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
      .expectHeader().contentType("application/json")
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessments-1.sql")
  fun `Post resettlement assessment submit - nomsId not found in database`() {
    val nomsId = "DEF1234"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/submit")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "pathway": "ACCOMMODATION",
          "assessmentType": "RESETTLEMENT_PLAN",
          "supportNeed": "SUPPORT_REQUIRED",
          "caseNoteSummary": "Testing 123"
        }
        """.trimIndent(),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessments-1.sql")
  fun `Post resettlement assessment submit - no assessment for nomsId found in database`() {
    val nomsId = "ABC1234"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/submit")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "pathway": "FINANCE_AND_ID",
          "assessmentType": "RESETTLEMENT_PLAN",
          "supportNeed": "SUPPORT_NOT_REQUIRED",
          "caseNoteSummary": "Testing 123"
        }
        """.trimIndent(),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessments-1.sql")
  fun `Post resettlement assessment submit - invalid input json`() {
    val nomsId = "DEF1234"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/submit")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "invalidField": "INFO"
        }
        """.trimIndent(),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType("application/json")
  }
}
