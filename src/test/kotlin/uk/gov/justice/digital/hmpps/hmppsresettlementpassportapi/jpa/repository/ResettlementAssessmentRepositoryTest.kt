package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
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
import java.time.LocalDate
import java.time.LocalDateTime

class ResettlementAssessmentRepositoryTest : RepositoryTestBase() {
  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @Autowired
  lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Test
  fun `test persist new resettlement assessment`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.parse("2022-12-20T10:13:03"), "crn1", "xyz1", LocalDate.parse("2025-01-23"))
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
      declarationBy = null,
      declarationDate = null,
    )
    resettlementAssessmentRepository.save(resettlementAssessmentQuestionAndAnswerList)

    val resettlementAssessmentsFromDB = resettlementAssessmentRepository.findAll()
    assertThat(resettlementAssessmentsFromDB).usingRecursiveComparison().isEqualTo(listOf(resettlementAssessmentQuestionAndAnswerList))
  }

  @Test
  fun `test findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc returns a ResettlementAssessmentEntity`() {
    val prisoner = PrisonerEntity(null, "NOM1234", LocalDateTime.parse("2022-12-20T10:13:03"), "crn1", "xyz1", LocalDate.parse("2025-01-23"))
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
      declarationBy = null,
      declarationDate = null,
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
      declarationBy = null,
      declarationDate = null,
    )
    resettlementAssessmentRepository.save(resettlementAssessment)
    resettlementAssessmentRepository.save(resettlementAssessment2)

    val resettlementAssessmentsFromDB = resettlementAssessmentRepository.findFirstByPrisonerIdAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisoner.id(), Pathway.ACCOMMODATION, ResettlementAssessmentType.RESETTLEMENT_PLAN)
    assertThat(resettlementAssessmentsFromDB).usingRecursiveComparison().isEqualTo(resettlementAssessment)
  }

  @Test
  fun `test assessment required custom queries`() {
    // Seed database with prisoners and resettlement statuses
    prisonerRepository.save(PrisonerEntity(null, "NOMS1", LocalDateTime.parse("2023-12-13T12:00:00"), "CRN1", "MDI", LocalDate.parse("2033-01-02")))
    val prisoner2 = prisonerRepository.save(PrisonerEntity(null, "NOMS2", LocalDateTime.parse("2023-12-13T12:00:00"), "CRN2", "MDI", LocalDate.parse("2043-09-16")))
    val prisoner3 = prisonerRepository.save(PrisonerEntity(null, "NOMS3", LocalDateTime.parse("2023-12-13T12:00:00"), "CRN3", "MDI", LocalDate.parse("2024-04-12")))
    val prisoner4 = prisonerRepository.save(PrisonerEntity(null, "NOMS4", LocalDateTime.parse("2023-12-13T12:00:00"), "CRN4", "MDI", LocalDate.parse("2030-07-11")))
    val prisoner5 = prisonerRepository.save(PrisonerEntity(null, "NOMS5", LocalDateTime.parse("2023-12-13T12:00:00"), "CRN5", "MDI", LocalDate.parse("2032-10-13")))

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

  fun generateResettlementAssessmentEntity(prisoner: PrisonerEntity, pathway: Pathway, status: ResettlementAssessmentStatus, creationDate: LocalDateTime) =
    ResettlementAssessmentEntity(
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
      declarationBy = null,
      declarationDate = null,
    )
}
