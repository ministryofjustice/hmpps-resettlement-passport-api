package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ProfileReset
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResetReason
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import java.time.LocalDateTime

class ProfileResetIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Autowired
  private lateinit var pathwayStatusRepository: PathwayStatusRepository
  
  private val fakeNow = LocalDateTime.parse("2024-10-01T12:00:00")

  @Test
  @Sql("classpath:testdata/sql/seed-profile-reset.sql")
  fun `POST reset profile - happy path`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow

    val nomsId = "123456"
    val prisonId = "MDI"
    val expectedCaseNotes = "TODO"

    caseNotesApiMockServer.stubPostCaseNotes(
      nomsId = nomsId,
      type = "RESET",
      subType = "GEN",
      text = expectedCaseNotes,
      prisonId = prisonId,
      status = 200,
    )

    authedWebTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/reset-profile")
      .bodyValue(
        ProfileReset(
          resetReason = ResetReason.OTHER,
          additionalDetails = "Some additional details",
        )
      )
      .exchange()
      .expectStatus().isOk

    val expectedPathwayStatuses = listOf(
      PathwayStatusEntity(id = 1, prisonerId = 1, pathway = Pathway.ACCOMMODATION, status = Status.NOT_STARTED, updatedDate = LocalDateTime.parse("2023-08-16T12:21:44.234")),
      PathwayStatusEntity(id = 2, prisonerId = 1, pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, status = Status.NOT_STARTED, updatedDate = LocalDateTime.parse("2023-08-16T12:21:44.234")),
      PathwayStatusEntity(id = 3, prisonerId = 1, pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, status = Status.NOT_STARTED, updatedDate = LocalDateTime.parse("2023-08-16T12:21:44.234")),
      PathwayStatusEntity(id = 4, prisonerId = 1, pathway = Pathway.DRUGS_AND_ALCOHOL, status = Status.NOT_STARTED, updatedDate = LocalDateTime.parse("2023-08-16T12:21:44.234")),
      PathwayStatusEntity(id = 5, prisonerId = 1, pathway = Pathway.EDUCATION_SKILLS_AND_WORK, status = Status.NOT_STARTED, updatedDate = LocalDateTime.parse("2023-08-16T12:21:44.234")),
      PathwayStatusEntity(id = 6, prisonerId = 1, pathway = Pathway.FINANCE_AND_ID, status = Status.NOT_STARTED, updatedDate = LocalDateTime.parse("2023-08-16T12:21:44.234")),
      PathwayStatusEntity(id = 7, prisonerId = 1, pathway = Pathway.HEALTH, status = Status.NOT_STARTED, updatedDate = LocalDateTime.parse("2023-08-16T12:21:44.234")),
    )

    val expectedResettlementAssessments = listOf(
      ResettlementAssessmentEntity(id = 1, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_NOT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = "Case note related to accommodation", createdByUserId = "USER_1", version = 1, submissionDate = null, userDeclaration = false, deleted = true, deletedDate = fakeNow),
      ResettlementAssessmentEntity(id = 2, prisonerId = 1, pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, statusChangedTo = Status.SUPPORT_DECLINED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = "Case note related to Attitudes, thinking and behaviour", createdByUserId = "USER_1", version = 1, submissionDate = null, userDeclaration = false, deleted = true, deletedDate = fakeNow),
      ResettlementAssessmentEntity(id = 3, prisonerId = 1, pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, statusChangedTo = Status.SUPPORT_NOT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = "Case note related to Children, family and communities", createdByUserId = "USER_1", version = 1, submissionDate = null, userDeclaration = false, deleted = true, deletedDate = fakeNow),
      ResettlementAssessmentEntity(id = 4, prisonerId = 1, pathway = Pathway.DRUGS_AND_ALCOHOL, statusChangedTo = Status.DONE, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = "Case note related to Drugs and alcohol", createdByUserId = "USER_1", version = 1, submissionDate = null, userDeclaration = false, deleted = true, deletedDate = fakeNow),
      ResettlementAssessmentEntity(id = 5, prisonerId = 1, pathway = Pathway.EDUCATION_SKILLS_AND_WORK, statusChangedTo = Status.SUPPORT_NOT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = "Case note related to education, skills and work", createdByUserId = "USER_1", version = 1, submissionDate = null, userDeclaration = false, deleted = true, deletedDate = fakeNow),
      ResettlementAssessmentEntity(id = 6, prisonerId = 1, pathway = Pathway.FINANCE_AND_ID, statusChangedTo = Status.IN_PROGRESS, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = "Case note related to Finance and ID", createdByUserId = "USER_1", version = 1, submissionDate = null, userDeclaration = false, deleted = true, deletedDate = fakeNow),
      ResettlementAssessmentEntity(id = 7, prisonerId = 1, pathway = Pathway.HEALTH, statusChangedTo = Status.NOT_STARTED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = "Case note related to Health", createdByUserId = "USER_1", version = 1, submissionDate = null, userDeclaration = false, deleted = true, deletedDate = fakeNow),
      ResettlementAssessmentEntity(id = 8, prisonerId = 1, pathway = Pathway.HEALTH, statusChangedTo = Status.NOT_STARTED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-01-09T20:02:45"), createdBy = "B User", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = "Case note related to Health", createdByUserId = "USER_2", version = 1, submissionDate = null, userDeclaration = false, deleted = true, deletedDate = fakeNow),
      ResettlementAssessmentEntity(id = 9, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_NOT_REQUIRED, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-01-12T10:23:59"), createdBy = "C User", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = "Case note related to Accommodation", createdByUserId = "USER_3", version = 1, submissionDate = null, userDeclaration = false, deleted = true, deletedDate = fakeNow),
    )

    Assertions.assertEquals(expectedPathwayStatuses, pathwayStatusRepository.findAll())
    Assertions.assertEquals(expectedResettlementAssessments, resettlementAssessmentRepository.findAll())

    unmockkAll()
  }

  @Test
  fun `POST reset profile - 400 invalid body`() {
    val nomsId = "123456"

    authedWebTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/reset-profile")
      .bodyValue("{}")
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `POST reset profile - 404 no prisoner found`() {
    val nomsId = "123456"

    authedWebTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/reset-profile")
      .bodyValue(
        ProfileReset(
          resetReason = ResetReason.OTHER,
          additionalDetails = "Some additional details",
        )
      )
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `POST reset profile - 401 unauthorised`() {
    val nomsId = "123456"

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/reset-profile")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `POST reset profile - 403 forbidden`() {
    val nomsId = "123456"

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/reset-profile")
      .headers(setAuthorisation())
      .exchange()
      .expectStatus().isForbidden
  }
}