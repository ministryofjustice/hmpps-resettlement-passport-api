package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.TestBase
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentSimpleQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
class ResettlementAssessmentRepositoryTest : TestBase() {
  @Autowired
  lateinit var prisonerRepository: PrisonerRepository

  @Autowired
  lateinit var pathwayRepository: PathwayRepository

  @Autowired
  lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Autowired
  lateinit var resettlementAssessmentStatusRepository: ResettlementAssessmentStatusRepository

  @BeforeEach
  @AfterEach
  fun beforeEach() {
    resettlementAssessmentRepository.deleteAll()
    prisonerRepository.deleteAll()
    pathwayRepository.deleteAll()
  }

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
      statusChangedTo = null,
      assessmentType = ResettlementAssessmentType.BCST2,
      creationDate = LocalDateTime.parse("2023-01-01T12:00:00"),
      createdBy = "Human, A",
      assessment = ResettlementAssessmentQuestionAndAnswerList(listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "TEST_QUESTION_1", answer = StringAnswer("Test Answer 1")))),
      assessmentStatus = assessmentStatus,
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
      statusChangedTo = null,
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
    )
    val resettlementAssessment2 = ResettlementAssessmentEntity(
      id = null,
      prisoner = prisoner,
      pathway = pathway,
      statusChangedTo = null,
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
    )
    resettlementAssessmentRepository.save(resettlementAssessment)
    resettlementAssessmentRepository.save(resettlementAssessment2)

    val resettlementAssessmentsFromDB = resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisoner, pathway, ResettlementAssessmentType.RESETTLEMENT_PLAN)
    assertThat(resettlementAssessmentsFromDB).usingRecursiveComparison().isEqualTo(resettlementAssessment)
  }
}
