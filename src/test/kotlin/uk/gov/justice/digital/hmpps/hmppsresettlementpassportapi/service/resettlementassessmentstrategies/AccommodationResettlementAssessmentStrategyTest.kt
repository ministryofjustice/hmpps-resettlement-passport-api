package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentpages.AccommodationAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentpages.AccommodationResettlementAssessmentQuestion
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class AccommodationResettlementAssessmentStrategyTest {
  private lateinit var resettlementAssessmentService: AccommodationResettlementAssessmentStrategy

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var pathwayRepository: PathwayRepository

  @Mock
  private lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Mock
  private lateinit var statusRepository: StatusRepository

  @Mock
  private lateinit var resettlementAssessmentStatusRepository: ResettlementAssessmentStatusRepository

  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")

  @BeforeEach
  fun beforeEach() {
    resettlementAssessmentService = AccommodationResettlementAssessmentStrategy(resettlementAssessmentRepository, prisonerRepository, statusRepository, pathwayRepository, resettlementAssessmentStatusRepository)
  }

  @Test
  fun `test next page returns WHO_WILL_THEY_LIVE_WITH when current page is WHERE_WILL_THEY_LIVE and answer is NEW_ADDRESS`() {
    var questions = mutableListOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
      ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE", answer = StringAnswer("NEW_ADDRESS")),
      ResettlementAssessmentRequestQuestionAndAnswer("WHAT_IS_THE_ADDRESS", answer = MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123")))),
    )
    val assessment = ResettlementAssessmentRequest(
      pathway = Pathway.ACCOMMODATION,
      nomsID = "123456",
      type = ResettlementAssessmentType.BCST2,
      currentPage = "WHERE_WILL_THEY_LIVE",
      questions = questions,
      newStatus = Status.NOT_STARTED,
    )
    var nextPage = resettlementAssessmentService.nextQuestions(assessment)
    Assertions.assertEquals(nextPage, AccommodationAssessmentPage.WHO_WILL_THEY_LIVE_WITH)
  }

  @Test
  fun `test next page returns CHECK_ANSWERS when current page is WHO_WILL_THEY_LIVE_WITH and that answers are included`() {
    val nomsId = "abc"
    val crn = "crn1"
    val prisonId = "xyz"
    val pathwayEntity = PathwayEntity(1, "Accommodation", true, testDate)
    val statusEntity = StatusEntity(2, "In Progress", true, testDate)
    val prisonerEntity = PrisonerEntity(1, nomsId, testDate, crn, prisonId, LocalDate.parse("2025-01-23"))
    val resettlementAssessmentStatusEntity = ResettlementAssessmentStatusEntity(1, "Not Started", true, LocalDateTime.now())
    val assessmentJsonString = "[{\"type\": \"string\", \"answer\": \"NEW_ADDRESS\", \"question\": \"WHERE_WILL_THEY_LIVE\"}, {\"type\": \"map\", \"answer\": [{\"addressLine1\": \"123 fake street\", \"city\": \"Leeds\", \"postcode\": \"LS1 123\"}], \"question\": \"WHAT_IS_THE_ADDRESS\"}, {\"type\": \"map\", \"answer\": [{\"name\": \"person1\", \"age\": \"47\"}, { \"name\": \"person2\", \"age\": \"53\"}], \"question\": \"WHO_WILL_THEY_LIVE_WITH\"}]"
    val resettlementAssessmentEntity = ResettlementAssessmentEntity(1, prisonerEntity, pathwayEntity, statusEntity, ResettlementAssessmentType.BCST2, assessmentJsonString, testDate, "", resettlementAssessmentStatusEntity)
    Mockito.`when`(pathwayRepository.findById(any())).thenReturn(Optional.of(pathwayEntity))
    Mockito.`when`(prisonerRepository.findByNomsId(any())).thenReturn(prisonerEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(any(), any(), any())).thenReturn(resettlementAssessmentEntity)

    var questions = mutableListOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
      ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE", answer = StringAnswer("NEW_ADDRESS")),
      ResettlementAssessmentRequestQuestionAndAnswer("WHAT_IS_THE_ADDRESS", answer = MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123")))),
      ResettlementAssessmentRequestQuestionAndAnswer(
        "WHO_WILL_THEY_LIVE_WITH",
        answer = MapAnswer(listOf(mapOf("name" to "person1", "age" to "47"), mapOf("name" to "person2", "age" to "53"))),
      ),
    )

    val expectedQuestions = mutableListOf(
      ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE, answer = StringAnswer("NEW_ADDRESS")),
      ResettlementAssessmentQuestionAndAnswer(AccommodationResettlementAssessmentQuestion.WHAT_IS_THE_ADDRESS, answer = MapAnswer(listOf(mapOf("addressLine1" to "123 fake street", "city" to "Leeds", "postcode" to "LS1 123")))),
      ResettlementAssessmentQuestionAndAnswer(
        AccommodationResettlementAssessmentQuestion.WHO_WILL_THEY_LIVE_WITH,
        answer = MapAnswer(listOf(mapOf("name" to "person1", "age" to "47"), mapOf("name" to "person2", "age" to "53"))),
      ),
    )
    val assessment = ResettlementAssessmentRequest(
      pathway = Pathway.ACCOMMODATION,
      nomsID = "abc",
      type = ResettlementAssessmentType.BCST2,
      currentPage = "WHO_WILL_THEY_LIVE_WITH",
      questions = questions,
      newStatus = Status.NOT_STARTED,
    )
    var nextPage = resettlementAssessmentService.nextQuestions(assessment)
    Assertions.assertEquals(nextPage, AccommodationAssessmentPage.CHECK_ANSWERS)
    Assertions.assertEquals(nextPage.questionsAndAnswers.first { it.question == AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE }.answer, expectedQuestions.first { it.question == AccommodationResettlementAssessmentQuestion.WHERE_WILL_THEY_LIVE }.answer)
    Assertions.assertEquals(nextPage.questionsAndAnswers.first { it.question == AccommodationResettlementAssessmentQuestion.WHAT_IS_THE_ADDRESS }.answer, expectedQuestions.first { it.question == AccommodationResettlementAssessmentQuestion.WHAT_IS_THE_ADDRESS }.answer)
    Assertions.assertEquals(nextPage.questionsAndAnswers.first { it.question == AccommodationResettlementAssessmentQuestion.WHO_WILL_THEY_LIVE_WITH }.answer, expectedQuestions.first { it.question == AccommodationResettlementAssessmentQuestion.WHO_WILL_THEY_LIVE_WITH }.answer)
  }
}
