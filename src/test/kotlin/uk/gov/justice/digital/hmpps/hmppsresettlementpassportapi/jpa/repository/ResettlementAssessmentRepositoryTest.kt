package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentSimpleQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.StatusEntity
import java.time.LocalDate
import java.time.LocalDateTime

class ResettlementAssessmentRepositoryTest : RepositoryTestBase() {
  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @Autowired
  lateinit var pathwayRepository: PathwayRepository

  @Autowired
  lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Autowired
  lateinit var resettlementAssessmentStatusRepository: ResettlementAssessmentStatusRepository

  @Autowired
  lateinit var statusRepository: StatusRepository

  @Test
  fun `test persist new resettlement assessment`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.parse("2022-12-20T10:13:03"), "crn1", "xyz1", LocalDate.parse("2025-01-23"))
    prisonerRepository.save(prisoner)

    val pathway = PathwayEntity(-1, "Accommodation", true, LocalDateTime.parse("2022-12-20T10:13:03"))
    pathwayRepository.save(pathway)

    val assessmentStatus = resettlementAssessmentStatusRepository.findById(ResettlementAssessmentStatus.NOT_STARTED.id).get()

    val resettlementAssessmentQuestionAndAnswerList = ResettlementAssessmentEntity(
      id = null,
      prisoner = prisoner,
      pathway = pathway,
      statusChangedTo = statusRepository.getReferenceById(Status.SUPPORT_DECLINED.id),
      assessmentType = ResettlementAssessmentType.BCST2,
      creationDate = LocalDateTime.parse("2023-01-01T12:00:00"),
      createdBy = "Human, A",
      assessment = ResettlementAssessmentQuestionAndAnswerList(listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "TEST_QUESTION_1", answer = StringAnswer("Test Answer 1")))),
      assessmentStatus = assessmentStatus,
      caseNoteText = "Some case note text",
      createdByUserId = "JSMITH_GEN",
    )
    resettlementAssessmentRepository.save(resettlementAssessmentQuestionAndAnswerList)

    val resettlementAssessmentsFromDB = resettlementAssessmentRepository.findAll()
    assertThat(resettlementAssessmentsFromDB).usingRecursiveComparison().isEqualTo(listOf(resettlementAssessmentQuestionAndAnswerList))
  }

  @Test
  fun `test findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc returns a ResettlementAssessmentEntity`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.parse("2022-12-20T10:13:03"), "crn1", "xyz1", LocalDate.parse("2025-01-23"))
    prisonerRepository.save(prisoner)

    val pathway = PathwayEntity(-1, "Accommodation", true, LocalDateTime.parse("2022-12-20T10:13:03"))
    pathwayRepository.save(pathway)

    val assessmentStatus = resettlementAssessmentStatusRepository.findById(ResettlementAssessmentStatus.NOT_STARTED.id).get()

    val resettlementAssessment = ResettlementAssessmentEntity(
      id = null,
      prisoner = prisoner,
      pathway = pathway,
      statusChangedTo = statusRepository.getReferenceById(Status.SUPPORT_NOT_REQUIRED.id),
      assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN,
      creationDate = LocalDateTime.parse("2023-01-01T12:00:00"),
      createdBy = "Human, A",
      assessment = ResettlementAssessmentQuestionAndAnswerList(
        listOf(
          ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "TEST_QUESTION_2", answer = StringAnswer("My answer")),
          ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "TEST_QUESTION_3", answer = ListAnswer(listOf("Answer 1", "Answer 2", "Answer 3"))),
        ),
      ),
      assessmentStatus = assessmentStatus,
      caseNoteText = "some case note text",
      createdByUserId = "JWILLIAMS_GEN",
    )
    val resettlementAssessment2 = ResettlementAssessmentEntity(
      id = null,
      prisoner = prisoner,
      pathway = pathway,
      statusChangedTo = statusRepository.getReferenceById(Status.NOT_STARTED.id),
      assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN,
      creationDate = LocalDateTime.parse("2022-01-01T12:00:00"),
      createdBy = "Human, A",
      assessment = ResettlementAssessmentQuestionAndAnswerList(
        listOf(
          ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "TEST_QUESTION_4", answer = MapAnswer(listOf(mapOf(Pair("key1", "value1"), Pair("key2", "value2"))))),
          ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "TEST_QUESTION_5", answer = ListAnswer(listOf("Answer 1", "Answer 2", "Answer 3"))),
        ),
      ),
      assessmentStatus = assessmentStatus,
      caseNoteText = "Some case notes",
      createdByUserId = "ABC1234",
    )
    resettlementAssessmentRepository.save(resettlementAssessment)
    resettlementAssessmentRepository.save(resettlementAssessment2)

    val resettlementAssessmentsFromDB = resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisoner, pathway, ResettlementAssessmentType.RESETTLEMENT_PLAN)
    assertThat(resettlementAssessmentsFromDB).usingRecursiveComparison().isEqualTo(resettlementAssessment)
  }

  @Test
  fun `test assessment required custom queries`() {
    // Seed database with prisoners and resettlement statuses
    val prisoner1 = PrisonerEntity(null, "NOMS1", LocalDateTime.parse("2023-12-13T12:00:00"), "CRN1", "MDI", LocalDate.parse("2033-01-02"))
    val prisoner2 = PrisonerEntity(null, "NOMS2", LocalDateTime.parse("2023-12-13T12:00:00"), "CRN2", "MDI", LocalDate.parse("2043-09-16"))
    val prisoner3 = PrisonerEntity(null, "NOMS3", LocalDateTime.parse("2023-12-13T12:00:00"), "CRN3", "MDI", LocalDate.parse("2024-04-12"))
    val prisoner4 = PrisonerEntity(null, "NOMS4", LocalDateTime.parse("2023-12-13T12:00:00"), "CRN4", "MDI", LocalDate.parse("2030-07-11"))
    val prisoner5 = PrisonerEntity(null, "NOMS5", LocalDateTime.parse("2023-12-13T12:00:00"), "CRN5", "MDI", LocalDate.parse("2032-10-13"))

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

    prisonerRepository.saveAll(listOf(prisoner1, prisoner2, prisoner3, prisoner4, prisoner5))
    resettlementAssessmentRepository.saveAll(resettlementAssessmentList)

    // Run query for each prisoner and ensure the number of pathways is correct
    Assertions.assertEquals(0, resettlementAssessmentRepository.countByNomsIdAndAssessmentTypeAndAssessmentStatus("NOMS1", ResettlementAssessmentType.BCST2, ResettlementAssessmentStatus.SUBMITTED.id))
    Assertions.assertEquals(7, resettlementAssessmentRepository.countByNomsIdAndAssessmentTypeAndAssessmentStatus("NOMS2", ResettlementAssessmentType.BCST2, ResettlementAssessmentStatus.SUBMITTED.id))
    Assertions.assertEquals(0, resettlementAssessmentRepository.countByNomsIdAndAssessmentTypeAndAssessmentStatus("NOMS3", ResettlementAssessmentType.BCST2, ResettlementAssessmentStatus.SUBMITTED.id))
    Assertions.assertEquals(7, resettlementAssessmentRepository.countByNomsIdAndAssessmentTypeAndAssessmentStatus("NOMS4", ResettlementAssessmentType.BCST2, ResettlementAssessmentStatus.SUBMITTED.id))
    Assertions.assertEquals(4, resettlementAssessmentRepository.countByNomsIdAndAssessmentTypeAndAssessmentStatus("NOMS5", ResettlementAssessmentType.BCST2, ResettlementAssessmentStatus.SUBMITTED.id))

    // Check we get the correct list of prisoners back
    Assertions.assertEquals(listOf(prisoner2, prisoner4), resettlementAssessmentRepository.findPrisonersWithAllAssessmentsInStatus("MDI", ResettlementAssessmentType.BCST2, ResettlementAssessmentStatus.SUBMITTED.id, Pathway.entries.size))
  }

  fun generateResettlementAssessmentEntity(prisoner: PrisonerEntity, pathway: Pathway, status: ResettlementAssessmentStatus, creationDate: LocalDateTime) =
    ResettlementAssessmentEntity(
      id = null,
      prisoner = prisoner,
      pathway = getPathwayEntity(pathway),
      statusChangedTo = getStatusEntity(Status.SUPPORT_NOT_REQUIRED),
      assessmentType = ResettlementAssessmentType.BCST2,
      assessment = ResettlementAssessmentQuestionAndAnswerList(listOf()),
      creationDate = creationDate,
      createdBy = "test user",
      assessmentStatus = getResettlementAssessmentStatusEntity(status),
      caseNoteText = "test",
      createdByUserId = "USER_1",
    )

  fun getPathwayEntity(pathway: Pathway): PathwayEntity = pathwayRepository.getReferenceById(pathway.id)
  fun getResettlementAssessmentStatusEntity(resettlementAssessmentStatus: ResettlementAssessmentStatus): ResettlementAssessmentStatusEntity = resettlementAssessmentStatusRepository.getReferenceById(resettlementAssessmentStatus.id)
  fun getStatusEntity(status: Status): StatusEntity = statusRepository.getReferenceById(status.id)
}
