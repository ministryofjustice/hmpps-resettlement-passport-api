package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentSimpleQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import java.time.LocalDateTime

class ResettlementAssessmentRepositoryTest : RepositoryTestBase() {
  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @Autowired
  lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Test
  fun `test persist new resettlement assessment`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.parse("2022-12-20T10:13:03"), "xyz1")
    prisonerRepository.save(prisoner)

    val resettlementAssessmentQuestionAndAnswerList = ResettlementAssessmentEntity(
      id = null,
      prisonerId = prisoner.id(),
      pathway = Pathway.ACCOMMODATION,
      statusChangedTo = Status.SUPPORT_DECLINED,
      assessmentType = ResettlementAssessmentType.BCST2,
      creationDate = LocalDateTime.parse("2023-01-01T12:00:00"),
      createdBy = "Human, A",
      assessment = ResettlementAssessmentQuestionAndAnswerList(listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "TEST_QUESTION_1", answer = StringAnswer("Test Answer 1")))),
      assessmentStatus = ResettlementAssessmentStatus.NOT_STARTED,
      caseNoteText = "Some case note text",
      createdByUserId = "JSMITH_GEN",
      submissionDate = null,
      version = 1,
      userDeclaration = false,
    )
    resettlementAssessmentRepository.save(resettlementAssessmentQuestionAndAnswerList)

    val resettlementAssessmentsFromDB = resettlementAssessmentRepository.findAll()
    assertThat(resettlementAssessmentsFromDB).usingRecursiveComparison().isEqualTo(listOf(resettlementAssessmentQuestionAndAnswerList))
  }

  @Test
  fun `test findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc returns a ResettlementAssessmentEntity`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.parse("2022-12-20T10:13:03"), "xyz1")
    prisonerRepository.save(prisoner)

    val resettlementAssessment = ResettlementAssessmentEntity(
      id = null,
      prisonerId = prisoner.id(),
      pathway = Pathway.ACCOMMODATION,
      statusChangedTo = Status.SUPPORT_NOT_REQUIRED,
      assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN,
      creationDate = LocalDateTime.parse("2023-01-01T12:00:00"),
      createdBy = "Human, A",
      assessment = ResettlementAssessmentQuestionAndAnswerList(
        listOf(
          ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "TEST_QUESTION_2", answer = StringAnswer("My answer")),
          ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "TEST_QUESTION_3", answer = ListAnswer(listOf("Answer 1", "Answer 2", "Answer 3"))),
        ),
      ),
      assessmentStatus = ResettlementAssessmentStatus.NOT_STARTED,
      caseNoteText = "some case note text",
      createdByUserId = "JWILLIAMS_GEN",
      submissionDate = null,
      version = 1,
      userDeclaration = false,
    )
    val resettlementAssessment2 = ResettlementAssessmentEntity(
      id = null,
      prisonerId = prisoner.id(),
      pathway = Pathway.ACCOMMODATION,
      statusChangedTo = Status.NOT_STARTED,
      assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN,
      creationDate = LocalDateTime.parse("2022-01-01T12:00:00"),
      createdBy = "Human, A",
      assessment = ResettlementAssessmentQuestionAndAnswerList(
        listOf(
          ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "TEST_QUESTION_4", answer = MapAnswer(listOf(mapOf(Pair("key1", "value1"), Pair("key2", "value2"))))),
          ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "TEST_QUESTION_5", answer = ListAnswer(listOf("Answer 1", "Answer 2", "Answer 3"))),
        ),
      ),
      assessmentStatus = ResettlementAssessmentStatus.NOT_STARTED,
      caseNoteText = "Some case notes",
      createdByUserId = "ABC1234",
      submissionDate = null,
      version = 1,
      userDeclaration = false,
    )
    resettlementAssessmentRepository.save(resettlementAssessment)
    resettlementAssessmentRepository.save(resettlementAssessment2)

    val resettlementAssessmentsFromDB = resettlementAssessmentRepository.findFirstByPrisonerIdAndPathwayAndAssessmentTypeAndDeletedIsFalseOrderByCreationDateDesc(prisoner.id(), Pathway.ACCOMMODATION, ResettlementAssessmentType.RESETTLEMENT_PLAN)
    assertThat(resettlementAssessmentsFromDB).usingRecursiveComparison().isEqualTo(resettlementAssessment)
  }

  @Test
  fun `test assessment required custom queries`() {
    // Seed database with prisoners and resettlement statuses
    prisonerRepository.save(PrisonerEntity(null, "NOMS1", LocalDateTime.parse("2023-12-13T12:00:00"), "MDI"))
    val prisoner2 = prisonerRepository.save(PrisonerEntity(null, "NOMS2", LocalDateTime.parse("2023-12-13T12:00:00"), "MDI"))
    val prisoner3 = prisonerRepository.save(PrisonerEntity(null, "NOMS3", LocalDateTime.parse("2023-12-13T12:00:00"), "MDI"))
    val prisoner4 = prisonerRepository.save(PrisonerEntity(null, "NOMS4", LocalDateTime.parse("2023-12-13T12:00:00"), "MDI"))
    val prisoner5 = prisonerRepository.save(PrisonerEntity(null, "NOMS5", LocalDateTime.parse("2023-12-13T12:00:00"), "MDI"))

    val resettlementAssessmentList = listOf(
      // Prisoner 1 has no assessments
      // Prisoner 2 has each assessment submitted once
      generateResettlementAssessmentEntity(prisoner2, Pathway.ACCOMMODATION, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-01T12:13:14")),
      generateResettlementAssessmentEntity(prisoner2, Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-01T12:13:14")),
      generateResettlementAssessmentEntity(prisoner2, Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-01T12:13:14")),
      generateResettlementAssessmentEntity(prisoner2, Pathway.FINANCE_AND_ID, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-01T12:13:14")),
      generateResettlementAssessmentEntity(prisoner2, Pathway.HEALTH, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-01T12:13:14")),
      generateResettlementAssessmentEntity(prisoner2, Pathway.DRUGS_AND_ALCOHOL, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-01T12:13:14")),
      generateResettlementAssessmentEntity(prisoner2, Pathway.EDUCATION_SKILLS_AND_WORK, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-01T12:13:14")),
      // Prisoner 3 has some assessments completed
      generateResettlementAssessmentEntity(prisoner3, Pathway.ACCOMMODATION, ResettlementAssessmentStatus.COMPLETE, LocalDateTime.parse("2023-05-01T12:13:14")),
      generateResettlementAssessmentEntity(prisoner3, Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, ResettlementAssessmentStatus.COMPLETE, LocalDateTime.parse("2023-05-01T12:13:14")),
      generateResettlementAssessmentEntity(prisoner3, Pathway.FINANCE_AND_ID, ResettlementAssessmentStatus.COMPLETE, LocalDateTime.parse("2023-05-01T12:13:14")),
      generateResettlementAssessmentEntity(prisoner3, Pathway.EDUCATION_SKILLS_AND_WORK, ResettlementAssessmentStatus.COMPLETE, LocalDateTime.parse("2023-05-01T12:13:14")),
      // Prisoner 4 has some assessments submitted multiple times
      generateResettlementAssessmentEntity(prisoner4, Pathway.ACCOMMODATION, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-01T12:13:14")),
      generateResettlementAssessmentEntity(prisoner4, Pathway.ACCOMMODATION, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-02T12:13:14")),
      generateResettlementAssessmentEntity(prisoner4, Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-01T12:13:14")),
      generateResettlementAssessmentEntity(prisoner4, Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-01T12:13:14")),
      generateResettlementAssessmentEntity(prisoner4, Pathway.FINANCE_AND_ID, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-01T12:13:14")),
      generateResettlementAssessmentEntity(prisoner4, Pathway.HEALTH, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-01T12:13:14")),
      generateResettlementAssessmentEntity(prisoner4, Pathway.HEALTH, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-02T12:13:14")),
      generateResettlementAssessmentEntity(prisoner4, Pathway.HEALTH, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-03T12:13:14")),
      generateResettlementAssessmentEntity(prisoner4, Pathway.DRUGS_AND_ALCOHOL, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-01T12:13:14")),
      generateResettlementAssessmentEntity(prisoner4, Pathway.EDUCATION_SKILLS_AND_WORK, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-01T12:13:14")),
      // Prisoner 5 (edge case) - not all pathways are submitted
      generateResettlementAssessmentEntity(prisoner5, Pathway.ACCOMMODATION, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-02T12:13:14")),
      generateResettlementAssessmentEntity(prisoner5, Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-01T12:13:14")),
      generateResettlementAssessmentEntity(prisoner5, Pathway.DRUGS_AND_ALCOHOL, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-01T12:13:14")),
      generateResettlementAssessmentEntity(prisoner5, Pathway.EDUCATION_SKILLS_AND_WORK, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.parse("2023-05-01T12:13:14")),
      generateResettlementAssessmentEntity(prisoner5, Pathway.HEALTH, ResettlementAssessmentStatus.COMPLETE, LocalDateTime.parse("2023-05-03T12:13:14")),
    )

    resettlementAssessmentRepository.saveAll(resettlementAssessmentList)

    // Run query for each prisoner and ensure the number of pathways is correct
    Assertions.assertEquals(0, resettlementAssessmentRepository.countByNomsIdAndAssessmentTypeAndAssessmentStatus("NOMS1", ResettlementAssessmentType.BCST2, ResettlementAssessmentStatus.SUBMITTED))
    Assertions.assertEquals(7, resettlementAssessmentRepository.countByNomsIdAndAssessmentTypeAndAssessmentStatus("NOMS2", ResettlementAssessmentType.BCST2, ResettlementAssessmentStatus.SUBMITTED))
    Assertions.assertEquals(0, resettlementAssessmentRepository.countByNomsIdAndAssessmentTypeAndAssessmentStatus("NOMS3", ResettlementAssessmentType.BCST2, ResettlementAssessmentStatus.SUBMITTED))
    Assertions.assertEquals(7, resettlementAssessmentRepository.countByNomsIdAndAssessmentTypeAndAssessmentStatus("NOMS4", ResettlementAssessmentType.BCST2, ResettlementAssessmentStatus.SUBMITTED))
    Assertions.assertEquals(4, resettlementAssessmentRepository.countByNomsIdAndAssessmentTypeAndAssessmentStatus("NOMS5", ResettlementAssessmentType.BCST2, ResettlementAssessmentStatus.SUBMITTED))

    // Check we get the correct list of prisoners back
    Assertions.assertEquals(setOf(prisoner2.id, prisoner4.id), resettlementAssessmentRepository.findPrisonersWithAllAssessmentsInStatus("MDI", ResettlementAssessmentType.BCST2, ResettlementAssessmentStatus.SUBMITTED, Pathway.entries.size))
  }

  @Test
  fun `test persist deleted resettlement assessment`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.parse("2022-12-20T10:13:03"), "xyz1")
    prisonerRepository.save(prisoner)

    val entityToSave = ResettlementAssessmentEntity(
      id = null,
      prisonerId = prisoner.id(),
      pathway = Pathway.ACCOMMODATION,
      statusChangedTo = Status.SUPPORT_DECLINED,
      assessmentType = ResettlementAssessmentType.BCST2,
      creationDate = LocalDateTime.parse("2023-01-01T12:00:00"),
      createdBy = "Human, A",
      assessment = ResettlementAssessmentQuestionAndAnswerList(listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "TEST_QUESTION_1", answer = StringAnswer("Test Answer 1")))),
      assessmentStatus = ResettlementAssessmentStatus.NOT_STARTED,
      caseNoteText = "Some case note text",
      createdByUserId = "TEST_GEN",
      submissionDate = null,
      version = 1,
      userDeclaration = false,
    )
    resettlementAssessmentRepository.save(entityToSave)

    val resettlementAssessmentsFromDB = resettlementAssessmentRepository.findAll()
    assertThat(resettlementAssessmentsFromDB).usingRecursiveComparison().isEqualTo(listOf(entityToSave))

    resettlementAssessmentRepository.delete(entityToSave)
    val afterDeleteFromDB = resettlementAssessmentRepository.findAll()
    assertThat(afterDeleteFromDB).isEmpty()
  }

  fun generateResettlementAssessmentEntity(prisoner: PrisonerEntity, pathway: Pathway, status: ResettlementAssessmentStatus, creationDate: LocalDateTime) = ResettlementAssessmentEntity(
    id = null,
    prisonerId = prisoner.id(),
    pathway = pathway,
    statusChangedTo = Status.SUPPORT_NOT_REQUIRED,
    assessmentType = ResettlementAssessmentType.BCST2,
    assessment = ResettlementAssessmentQuestionAndAnswerList(listOf()),
    creationDate = creationDate,
    createdBy = "test user",
    assessmentStatus = status,
    caseNoteText = "test",
    createdByUserId = "USER_1",
    submissionDate = null,
    version = 1,
    userDeclaration = false,
  )

  @Test
  fun `test findFirstByPrisonerIdAndAssessmentStatusAndDeletedIsFalseAndSubmissionDateIsNotNullOrderBySubmissionDateDesc - no results`() {
    Assertions.assertNull(resettlementAssessmentRepository.findFirstByPrisonerIdAndAssessmentStatusAndDeletedIsFalseAndSubmissionDateIsNotNullOrderBySubmissionDateDesc(1, ResettlementAssessmentStatus.SUBMITTED))
  }

  @Test
  fun `test findAllByPrisonerIdAndCreationDateBetween returns correct data`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.parse("2022-12-20T10:13:03"), "xyz1")
    prisonerRepository.save(prisoner)

    val assessment1 = generateResettlementAssessmentEntity(prisoner, Pathway.ACCOMMODATION, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.now())
    val assessment2 = generateResettlementAssessmentEntity(prisoner, Pathway.DRUGS_AND_ALCOHOL, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.now())
    val assessment3 = generateResettlementAssessmentEntity(prisoner, Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.now().minusDays(2))
    val assessment4 = generateResettlementAssessmentEntity(prisoner, Pathway.FINANCE_AND_ID, ResettlementAssessmentStatus.SUBMITTED, LocalDateTime.now().plusDays(2))

    resettlementAssessmentRepository.saveAll(listOf(assessment1, assessment2, assessment3, assessment4))

    val result = resettlementAssessmentRepository.findAllByPrisonerIdAndCreationDateBetween(prisoner.id(), LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1))

    org.assertj.core.api.Assertions.assertThat(result).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(listOf(assessment1, assessment2))
  }

  @Test
  fun `test findAllByPrisonerIdAndCreationDateBetween returns an empty list if no data found`() {
    val result = resettlementAssessmentRepository.findAllByPrisonerIdAndCreationDateBetween(1, LocalDateTime.now(), LocalDateTime.now())

    org.assertj.core.api.Assertions.assertThat(result).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java).isEqualTo(emptyList<ResettlementAssessmentEntity>())
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-18.sql")
  fun `test findFirstByPrisonerIdAndAssessmentStatusAndDeletedIsFalseAndSubmissionDateIsNotNullOrderBySubmissionDateDesc`() {
    val expectedResettlementAssessment = ResettlementAssessmentEntity(id = 9, prisonerId = 1, pathway = Pathway.HEALTH, statusChangedTo = Status.NOT_STARTED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf()), creationDate = LocalDateTime.parse("2023-01-09T19:02:45"), createdBy = "A User", assessmentStatus = ResettlementAssessmentStatus.SUBMITTED, caseNoteText = null, createdByUserId = "USER_1", version = 1, submissionDate = LocalDateTime.parse("2023-01-09T21:02:45"), userDeclaration = false, deleted = false, deletedDate = null)
    Assertions.assertEquals(expectedResettlementAssessment, resettlementAssessmentRepository.findFirstByPrisonerIdAndAssessmentStatusAndDeletedIsFalseAndSubmissionDateIsNotNullOrderBySubmissionDateDesc(1, ResettlementAssessmentStatus.SUBMITTED))
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-19.sql")
  fun `test findLastReportByPrison`() {
    val expectedLastReports = listOf(
      LastReportProjectionDataClass(nomsId = "A8132DY", assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, createdDate = LocalDateTime.parse("2023-05-18T12:21:44"), submissionDate = LocalDateTime.parse("2023-05-18T12:21:44")),
      LastReportProjectionDataClass(nomsId = "A8229DY", assessmentType = ResettlementAssessmentType.BCST2, createdDate = LocalDateTime.parse("2023-05-19T12:21:44"), submissionDate = LocalDateTime.parse("2023-05-19T12:21:44")),
      LastReportProjectionDataClass(nomsId = "A8257DY", assessmentType = ResettlementAssessmentType.BCST2, createdDate = LocalDateTime.parse("2023-05-19T12:21:44"), submissionDate = LocalDateTime.parse("2023-05-19T12:21:44")),
      LastReportProjectionDataClass(nomsId = "A8258DY", assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, createdDate = LocalDateTime.parse("2023-05-19T12:21:44"), submissionDate = LocalDateTime.parse("2023-05-19T12:21:44")),
      LastReportProjectionDataClass(nomsId = "A8314DY", assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, createdDate = LocalDateTime.parse("2023-05-17T12:21:44"), submissionDate = LocalDateTime.parse("2023-05-17T12:21:44")),
      LastReportProjectionDataClass(nomsId = "A8339DY", assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, createdDate = LocalDateTime.parse("2023-05-19T12:21:44"), submissionDate = LocalDateTime.parse("2023-05-19T12:21:44")),
      LastReportProjectionDataClass(nomsId = "G1458GV", assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, createdDate = LocalDateTime.parse("2023-05-16T12:21:44"), submissionDate = LocalDateTime.parse("2023-05-16T12:21:44")),
      LastReportProjectionDataClass(nomsId = "G6335VX", assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, createdDate = LocalDateTime.parse("2023-05-14T12:21:44"), submissionDate = null),
      LastReportProjectionDataClass(nomsId = "G6628UE", assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, createdDate = LocalDateTime.parse("2023-05-17T12:21:44"), submissionDate = LocalDateTime.parse("2023-05-17T12:21:44")),
      LastReportProjectionDataClass(nomsId = "G6933GF", assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, createdDate = LocalDateTime.parse("2023-05-18T12:21:44"), submissionDate = LocalDateTime.parse("2023-05-18T12:21:44")),
    )
    val actualLastReports = resettlementAssessmentRepository.findLastReportByPrison("MDI").convertToDataClass()
    Assertions.assertEquals(expectedLastReports, actualLastReports)
  }

  fun List<LastReportProjection>.convertToDataClass() = this.map { LastReportProjectionDataClass(it.nomsId, it.assessmentType, it.createdDate, it.submissionDate) }

  data class LastReportProjectionDataClass(
    val nomsId: String,
    val assessmentType: ResettlementAssessmentType,
    val createdDate: LocalDateTime,
    val submissionDate: LocalDateTime?,
  )
}
