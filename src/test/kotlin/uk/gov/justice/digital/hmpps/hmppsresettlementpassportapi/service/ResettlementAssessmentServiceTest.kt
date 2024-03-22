package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.PrisonerResettlementAssessment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.StatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentStatusRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class ResettlementAssessmentServiceTest {
  private lateinit var resettlementAssessmentService: ResettlementAssessmentService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var pathwayRepository: PathwayRepository

  @Mock
  private lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Mock
  private lateinit var resettlementAssessmentStatusRepository: ResettlementAssessmentStatusRepository

  @Mock
  private lateinit var deliusContactService: DeliusContactService

  @Mock
  private lateinit var caseNotesService: CaseNotesService

  @Mock
  private lateinit var pathwayAndStatusService: PathwayAndStatusService

  @Mock
  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    resettlementAssessmentService = ResettlementAssessmentService(resettlementAssessmentRepository, resettlementAssessmentStatusRepository, prisonerRepository, pathwayRepository, deliusContactService, caseNotesService, pathwayAndStatusService)
  }

  @Test
  fun `test getResettlementAssessmentSummaryByNomsId - returns assessment- combination of not started and complete`() {
    val nomsId: String = "GY3245"
    val assessmentType = ResettlementAssessmentType.BCST2
    val prisonerEntity = PrisonerEntity(1, "GY3245", testDate, "crn", "xyz1", LocalDate.parse("2025-01-23"))
    val accommodationPathwayEntity = PathwayEntity(1, "Accommodation", true, fakeNow)
    val accommodationResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(1, "Accommodation")
    val attitudesPathwayEntity = PathwayEntity(2, "Attitudes, thinking and behaviour", true, fakeNow)
    val attitudesResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(2, "Attitudes, thinking and behaviour")
    val childrenFamiliesPathwayEntity = PathwayEntity(3, "Children, families and communities", true, fakeNow)
    val childrenFamiliesResettlementAssessmentEntity = createCompleteResettlementAssessmentEntity(3, "Children, families and communities")
    val drugsAlcoholPathwayEntity = PathwayEntity(4, "Drugs and alcohol", true, fakeNow)
    val drugsAlcoholResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(4, "Drugs and alcohol")
    val educationSkillsPathwayEntity = PathwayEntity(5, "Education, skills and work", true, fakeNow)
    val educationSkillsResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(5, "Education, skills and work")
    val financeIdPathwayEntity = PathwayEntity(6, "Finance and ID", true, fakeNow)
    val financeIdResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(6, "Finance and ID")
    val healthPathwayEntity = PathwayEntity(7, "Health", true, fakeNow)
    val healthResettlementAssessmentEntity = createCompleteResettlementAssessmentEntity(7, "Health")

    val prisonerResettlementAssessmentSummary = listOf(
      PrisonerResettlementAssessment(Pathway.ACCOMMODATION, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, ResettlementAssessmentStatus.COMPLETE),
      PrisonerResettlementAssessment(Pathway.DRUGS_AND_ALCOHOL, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.EDUCATION_SKILLS_AND_WORK, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.FINANCE_AND_ID, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.HEALTH, ResettlementAssessmentStatus.COMPLETE),
    )

    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    Mockito.`when`(pathwayRepository.findById(Pathway.ACCOMMODATION.id)).thenReturn(Optional.of(accommodationPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR.id)).thenReturn(Optional.of(attitudesPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY.id)).thenReturn(Optional.of(childrenFamiliesPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.DRUGS_AND_ALCOHOL.id)).thenReturn(Optional.of(drugsAlcoholPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.EDUCATION_SKILLS_AND_WORK.id)).thenReturn(Optional.of(educationSkillsPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.FINANCE_AND_ID.id)).thenReturn(Optional.of(financeIdPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.HEALTH.id)).thenReturn(Optional.of(healthPathwayEntity))
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, accommodationPathwayEntity, assessmentType)).thenReturn(accommodationResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, attitudesPathwayEntity, assessmentType)).thenReturn(attitudesResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, childrenFamiliesPathwayEntity, assessmentType)).thenReturn(childrenFamiliesResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, drugsAlcoholPathwayEntity, assessmentType)).thenReturn(drugsAlcoholResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, educationSkillsPathwayEntity, assessmentType)).thenReturn(educationSkillsResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, financeIdPathwayEntity, assessmentType)).thenReturn(financeIdResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, healthPathwayEntity, assessmentType)).thenReturn(healthResettlementAssessmentEntity)

    val response = resettlementAssessmentService.getResettlementAssessmentSummaryByNomsId(nomsId, ResettlementAssessmentType.BCST2)
    Assertions.assertEquals(prisonerResettlementAssessmentSummary, response)
  }

  @Test
  fun `test getResettlementAssessmentSummaryByNomsId with BCST2 type - returns assessment with not started statuses for pathways with null value in resettlement_assessment table`() {
    val nomsId: String = "GY3245"
    val assessmentType = ResettlementAssessmentType.BCST2
    val prisonerEntity = PrisonerEntity(1, "GY3245", testDate, "crn", "xyz1", LocalDate.parse("2025-01-23"))
    val accommodationPathwayEntity = PathwayEntity(1, "Accommodation", true, fakeNow)
    val accommodationResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(1, "Accommodation")
    val attitudesPathwayEntity = PathwayEntity(2, "Attitudes, thinking and behaviour", true, fakeNow)
    val attitudesResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(2, "Attitudes, thinking and behaviour")
    val childrenFamiliesPathwayEntity = PathwayEntity(3, "Children, families and communities", true, fakeNow)
    val drugsAlcoholPathwayEntity = PathwayEntity(4, "Drugs and alcohol", true, fakeNow)
    val drugsAlcoholResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(4, "Drugs and alcohol")
    val educationSkillsPathwayEntity = PathwayEntity(5, "Education, skills and work", true, fakeNow)
    val educationSkillsResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(5, "Education, skills and work")
    val financeIdPathwayEntity = PathwayEntity(6, "Finance and ID", true, fakeNow)
    val healthPathwayEntity = PathwayEntity(7, "Health", true, fakeNow)
    val healthResettlementAssessmentEntity = createCompleteResettlementAssessmentEntity(7, "Health")

    val prisonerResettlementAssessmentSummary = listOf(
      PrisonerResettlementAssessment(Pathway.ACCOMMODATION, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.DRUGS_AND_ALCOHOL, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.EDUCATION_SKILLS_AND_WORK, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.FINANCE_AND_ID, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.HEALTH, ResettlementAssessmentStatus.COMPLETE),
    )

    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    Mockito.`when`(pathwayRepository.findById(Pathway.ACCOMMODATION.id)).thenReturn(Optional.of(accommodationPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR.id)).thenReturn(Optional.of(attitudesPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY.id)).thenReturn(Optional.of(childrenFamiliesPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.DRUGS_AND_ALCOHOL.id)).thenReturn(Optional.of(drugsAlcoholPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.EDUCATION_SKILLS_AND_WORK.id)).thenReturn(Optional.of(educationSkillsPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.FINANCE_AND_ID.id)).thenReturn(Optional.of(financeIdPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.HEALTH.id)).thenReturn(Optional.of(healthPathwayEntity))
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, accommodationPathwayEntity, assessmentType)).thenReturn(accommodationResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, attitudesPathwayEntity, assessmentType)).thenReturn(attitudesResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, childrenFamiliesPathwayEntity, assessmentType)).thenReturn(null)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, drugsAlcoholPathwayEntity, assessmentType)).thenReturn(drugsAlcoholResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, educationSkillsPathwayEntity, assessmentType)).thenReturn(educationSkillsResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, financeIdPathwayEntity, assessmentType)).thenReturn(null)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, healthPathwayEntity, assessmentType)).thenReturn(healthResettlementAssessmentEntity)

    val bcst2Response = resettlementAssessmentService.getResettlementAssessmentSummaryByNomsId(nomsId, ResettlementAssessmentType.BCST2)
    Assertions.assertEquals(prisonerResettlementAssessmentSummary, bcst2Response)
  }

  @Test
  fun `test getResettlementAssessmentSummaryByNomsId with RESETTLEMENT_PLAN type - returns assessment with not started statuses for pathways with null value in resettlement_assessment`() {
    val nomsId: String = "GY3245"
    val assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN
    val prisonerEntity = PrisonerEntity(1, "GY3245", testDate, "crn", "xyz1", LocalDate.parse("2025-01-23"))
    val accommodationPathwayEntity = PathwayEntity(1, "Accommodation", true, fakeNow)
    val accommodationResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(1, "Accommodation")
    val attitudesPathwayEntity = PathwayEntity(2, "Attitudes, thinking and behaviour", true, fakeNow)
    val attitudesResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(2, "Attitudes, thinking and behaviour")
    val childrenFamiliesPathwayEntity = PathwayEntity(3, "Children, families and communities", true, fakeNow)
    val drugsAlcoholPathwayEntity = PathwayEntity(4, "Drugs and alcohol", true, fakeNow)
    val drugsAlcoholResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(4, "Drugs and alcohol")
    val educationSkillsPathwayEntity = PathwayEntity(5, "Education, skills and work", true, fakeNow)
    val educationSkillsResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(5, "Education, skills and work")
    val financeIdPathwayEntity = PathwayEntity(6, "Finance and ID", true, fakeNow)
    val healthPathwayEntity = PathwayEntity(7, "Health", true, fakeNow)
    val healthResettlementAssessmentEntity = createCompleteResettlementAssessmentEntity(7, "Health")

    val prisonerResettlementAssessmentSummary = listOf(
      PrisonerResettlementAssessment(Pathway.ACCOMMODATION, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.DRUGS_AND_ALCOHOL, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.EDUCATION_SKILLS_AND_WORK, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.FINANCE_AND_ID, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.HEALTH, ResettlementAssessmentStatus.COMPLETE),
    )

    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    Mockito.`when`(pathwayRepository.findById(Pathway.ACCOMMODATION.id)).thenReturn(Optional.of(accommodationPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR.id)).thenReturn(Optional.of(attitudesPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY.id)).thenReturn(Optional.of(childrenFamiliesPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.DRUGS_AND_ALCOHOL.id)).thenReturn(Optional.of(drugsAlcoholPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.EDUCATION_SKILLS_AND_WORK.id)).thenReturn(Optional.of(educationSkillsPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.FINANCE_AND_ID.id)).thenReturn(Optional.of(financeIdPathwayEntity))
    Mockito.`when`(pathwayRepository.findById(Pathway.HEALTH.id)).thenReturn(Optional.of(healthPathwayEntity))
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, accommodationPathwayEntity, assessmentType)).thenReturn(accommodationResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, attitudesPathwayEntity, assessmentType)).thenReturn(attitudesResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, childrenFamiliesPathwayEntity, assessmentType)).thenReturn(null)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, drugsAlcoholPathwayEntity, assessmentType)).thenReturn(drugsAlcoholResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, educationSkillsPathwayEntity, assessmentType)).thenReturn(educationSkillsResettlementAssessmentEntity)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, financeIdPathwayEntity, assessmentType)).thenReturn(null)
    Mockito.`when`(resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeOrderByCreationDateDesc(prisonerEntity, healthPathwayEntity, assessmentType)).thenReturn(healthResettlementAssessmentEntity)

    val resettlementResponse = resettlementAssessmentService.getResettlementAssessmentSummaryByNomsId(nomsId, ResettlementAssessmentType.RESETTLEMENT_PLAN)
    Assertions.assertEquals(prisonerResettlementAssessmentSummary, resettlementResponse)
  }

  @ParameterizedTest
  @MethodSource("test convertAnswerToString data")
  fun `test convertAnswerToString`(type: TypeOfQuestion, options: List<Option>?, answer: Answer<*>, expectedString: String) {
    Assertions.assertEquals(expectedString, resettlementAssessmentService.convertAnswerToString(options, answer))
  }

  private fun `test convertAnswerToString data`() = Stream.of(
    Arguments.of(TypeOfQuestion.LONG_TEXT, null, StringAnswer("My answer"), "My answer"),
    Arguments.of(TypeOfQuestion.SHORT_TEXT, null, StringAnswer("My answer"), "My answer"),
    Arguments.of(TypeOfQuestion.RADIO, listOf(Option("MY_ANSWER", "My answer"), Option("OTHER_OPTION", "Other option")), StringAnswer("MY_ANSWER"), "My answer"),
    Arguments.of(TypeOfQuestion.RADIO, null, StringAnswer("MY_ANSWER"), "MY_ANSWER"),
    Arguments.of(TypeOfQuestion.ADDRESS, null, MapAnswer(listOf(mapOf("Address line 1" to "123 Main Street"), mapOf("Address line 2" to "Leeds"), mapOf("County" to "West Yorkshire"), mapOf("Postcode" to "LS1 1AB", "Country" to "United Kingdom"))), "123 Main Street\nLeeds\nWest Yorkshire\nLS1 1AB\nUnited Kingdom"),
    Arguments.of(TypeOfQuestion.ADDRESS, null, MapAnswer(listOf(mapOf(), mapOf(), mapOf(), mapOf())), ""),
    Arguments.of(TypeOfQuestion.ADDRESS, null, MapAnswer(listOf()), ""),
    Arguments.of(TypeOfQuestion.CHECKBOX, listOf(Option("ANSWER_1", "Answer 1"), Option("ANSWER_2", "Answer 2")), ListAnswer(listOf("ANSWER_1", "ANSWER_2", "ANSWER_3")), "Answer 1\nAnswer 2\nANSWER_3"),
    Arguments.of(TypeOfQuestion.CHECKBOX, null, ListAnswer(listOf()), ""),
  )

  private fun createNotStartedResettlementAssessmentEntity(id: Long, name: String) = ResettlementAssessmentEntity(
    id = id,
    prisoner = PrisonerEntity(1, "GY3245", testDate, "crn", "xyz1", LocalDate.parse("2025-01-23")),
    pathway = PathwayEntity(id = id, name = name, active = true, fakeNow),
    assessmentType = ResettlementAssessmentType.BCST2,
    assessmentStatus = ResettlementAssessmentStatusEntity(id = 1, name = "Not Started", true, fakeNow),
    assessment = ResettlementAssessmentQuestionAndAnswerList(listOf()),
    creationDate = fakeNow,
    createdBy = "PO",
    statusChangedTo = StatusEntity(id = 1, name = "Not Started", active = true, creationDate = fakeNow),
    caseNoteText = "some case note text",
    createdByUserId = "USER_1",
  )

  private fun createCompleteResettlementAssessmentEntity(id: Long, name: String) = ResettlementAssessmentEntity(
    id = id,
    prisoner = PrisonerEntity(1, "GY3245", testDate, "crn", "xyz1", LocalDate.parse("2025-01-23")),
    pathway = PathwayEntity(id = id, name = name, active = true, fakeNow),
    assessmentType = ResettlementAssessmentType.BCST2,
    assessmentStatus = ResettlementAssessmentStatusEntity(id = 3, name = "Complete", true, fakeNow),
    assessment = ResettlementAssessmentQuestionAndAnswerList(listOf()),
    creationDate = fakeNow,
    createdBy = "PO",
    statusChangedTo = StatusEntity(id = 4, name = "Support Declined", active = true, creationDate = fakeNow),
    caseNoteText = "some case note text",
    createdByUserId = "USER_1",
  )
}
