package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ProfileReset
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResetReason
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.SupportNeedStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerSupportNeedUpdateEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerSupportNeedRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerSupportNeedUpdateRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.SupportNeedRepository
import java.time.LocalDateTime

class ProfileResetIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Autowired
  private lateinit var pathwayStatusRepository: PathwayStatusRepository

  @Autowired
  private lateinit var prisonerRepository: PrisonerRepository

  @Autowired
  private lateinit var prisonerSupportNeedRepository: PrisonerSupportNeedRepository

  @Autowired
  private lateinit var prisonerSupportNeedUpdateRepository: PrisonerSupportNeedUpdateRepository

  @Autowired
  private lateinit var supportNeedRepository: SupportNeedRepository

  private val fakeNow = LocalDateTime.parse("2024-10-01T12:00:00")

  @Test
  @Sql("classpath:testdata/sql/seed-profile-reset.sql")
  fun `POST reset profile - happy path`() {
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow

    val nomsId = "ABC1234"
    val prisonId = "MDI"
    val expectedCaseNotes = "Prepare someone for release reports and statuses reset\\n\\n" +
      "Reason for reset: Some additional details\\n\\n" +
      "Any previous immediate needs and pre-release reports have been saved in our archive, but are no longer visible in PSfR.\\n\\n" +
      "All pathway resettlement statuses have been set back to 'Not Started'.\\n\\n" +
      "Contact the service desk if you think there's a problem."

    caseNotesApiMockServer.stubPostCaseNotes(
      nomsId = nomsId,
      type = "RESET",
      subType = "GEN",
      text = expectedCaseNotes,
      prisonId = prisonId,
      status = 200,
    )

    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/reset-profile")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), authSource = "nomis"))
      .bodyValue(
        ProfileReset(
          resetReason = ResetReason.OTHER,
          additionalDetails = "Some additional details",
        ),
      )
      .exchange()
      .expectStatus().isOk

    val expectedPathwayStatuses = listOf(
      PathwayStatusEntity(id = 1, prisonerId = 1, pathway = Pathway.ACCOMMODATION, status = Status.NOT_STARTED, updatedDate = fakeNow),
      PathwayStatusEntity(id = 2, prisonerId = 1, pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, status = Status.NOT_STARTED, updatedDate = fakeNow),
      PathwayStatusEntity(id = 3, prisonerId = 1, pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, status = Status.NOT_STARTED, updatedDate = fakeNow),
      PathwayStatusEntity(id = 4, prisonerId = 1, pathway = Pathway.DRUGS_AND_ALCOHOL, status = Status.NOT_STARTED, updatedDate = fakeNow),
      PathwayStatusEntity(id = 5, prisonerId = 1, pathway = Pathway.EDUCATION_SKILLS_AND_WORK, status = Status.NOT_STARTED, updatedDate = fakeNow),
      PathwayStatusEntity(id = 6, prisonerId = 1, pathway = Pathway.FINANCE_AND_ID, status = Status.NOT_STARTED, updatedDate = fakeNow),
      PathwayStatusEntity(id = 7, prisonerId = 1, pathway = Pathway.HEALTH, status = Status.NOT_STARTED, updatedDate = fakeNow),
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

    val auditQueueMessage = sqsClient.receiveMessage(ReceiveMessageRequest.builder().queueUrl(auditQueueUrl).build()).get().messages()[0]
    assertThat(ObjectMapper().readValue(auditQueueMessage.body(), Map::class.java))
      .usingRecursiveComparison()
      .ignoringFields("when")
      .isEqualTo(mapOf("correlationId" to null, "details" to null, "service" to "hmpps-resettlement-passport-api", "subjectId" to "ABC1234", "subjectType" to "PRISONER_ID", "what" to "RESET_PROFILE", "when" to "2025-01-06T13:48:20.391273Z", "who" to "RESETTLEMENTPASSPORT_ADM"))

    // Prisoner should have supportNeedsLegacyProfile set to false
    val expectedPrisoner = PrisonerEntity(1, "ABC1234", LocalDateTime.parse("2023-08-16T12:21:38.709"), "MDI", false)
    Assertions.assertEquals(expectedPrisoner, prisonerRepository.findById(1).get())

    // Check prisoner support needs have been deleted and updates added
    val expectedPrisonerSupportNeeds = listOf(
      PrisonerSupportNeedEntity(id = 1, prisonerId = 1, supportNeed = supportNeedRepository.findById(1).get(), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = true, deletedDate = LocalDateTime.parse("2024-02-21T09:37:28.713421"), latestUpdateId = null),
      PrisonerSupportNeedEntity(id = 2, prisonerId = 1, supportNeed = supportNeedRepository.findById(1).get(), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = true, deletedDate = fakeNow, latestUpdateId = null),
      PrisonerSupportNeedEntity(id = 3, prisonerId = 1, supportNeed = supportNeedRepository.findById(7).get(), otherDetail = null, createdBy = "Someone", createdDate = LocalDateTime.parse("2024-02-21T09:36:28.713421"), deleted = true, deletedDate = fakeNow, latestUpdateId = null),
    )
    Assertions.assertEquals(expectedPrisonerSupportNeeds, prisonerSupportNeedRepository.findAll().sortedBy { it.id })

    val expectedPrisonerSupportNeedUpdates = listOf(
      PrisonerSupportNeedUpdateEntity(id = 1, prisonerSupportNeedId = 2, createdBy = "RESETTLEMENTPASSPORT_ADM", createdDate = fakeNow, updateText = "Support need removed because of profile reset\n\nReason for reset: Some additional details", status = null, isPrison = false, isProbation = false, deleted = false, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 101, prisonerSupportNeedId = 2, createdBy = "A user", createdDate = LocalDateTime.parse("2024-02-22T09:36:32.713421"), updateText = "This is an update 1", status = SupportNeedStatus.MET, isPrison = true, isProbation = true, deleted = true, deletedDate = null),
      PrisonerSupportNeedUpdateEntity(id = 102, prisonerSupportNeedId = 2, createdBy = "A user", createdDate = LocalDateTime.parse("2024-02-22T09:36:30.713421"), updateText = "This is an update 2", status = SupportNeedStatus.IN_PROGRESS, isPrison = true, isProbation = false, deleted = false, deletedDate = null),
    )
    Assertions.assertEquals(expectedPrisonerSupportNeedUpdates, prisonerSupportNeedUpdateRepository.findAll().sortedBy { it.id })

    unmockkAll()
  }

  @Test
  fun `POST reset profile - 400 invalid body`() {
    val nomsId = "ABC1234"

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/reset-profile")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), authSource = "nomis"))
      .bodyValue("{}")
      .exchange()
      .expectStatus().isBadRequest
  }

  @Test
  fun `POST reset profile - 404 no prisoner found`() {
    val nomsId = "ABC1234"

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/reset-profile")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), authSource = "nomis"))
      .bodyValue(
        ProfileReset(
          resetReason = ResetReason.OTHER,
          additionalDetails = "Some additional details",
        ),
      )
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `POST reset profile - 401 unauthorised`() {
    val nomsId = "ABC1234"

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/reset-profile")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `POST reset profile - 403 forbidden`() {
    val nomsId = "ABC1234"

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/reset-profile")
      .headers(setAuthorisation())
      .bodyValue(
        ProfileReset(
          resetReason = ResetReason.OTHER,
          additionalDetails = "Some additional details",
        ),
      )
      .exchange()
      .expectStatus().isForbidden
  }
}
