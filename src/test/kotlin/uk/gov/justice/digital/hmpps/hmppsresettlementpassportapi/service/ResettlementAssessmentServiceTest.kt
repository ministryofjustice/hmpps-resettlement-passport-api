package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.PrisonerResettlementAssessment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.AssessmentSkipRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class ResettlementAssessmentServiceTest {
  private lateinit var resettlementAssessmentService: ResettlementAssessmentService

  @Mock
  private lateinit var prisonerRepository: PrisonerRepository

  @Mock
  private lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository

  @Mock
  private lateinit var caseNotesService: CaseNotesService

  @Mock
  private lateinit var pathwayAndStatusService: PathwayAndStatusService

  @Mock
  private lateinit var assessmentSkipRepository: AssessmentSkipRepository

  @Mock
  private val testDate = LocalDateTime.parse("2023-08-16T12:00:00")
  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @BeforeEach
  fun beforeEach() {
    resettlementAssessmentService = ResettlementAssessmentService(
      resettlementAssessmentRepository,
      prisonerRepository,
      caseNotesService,
      pathwayAndStatusService,
      assessmentSkipRepository,
    )
  }

  @Test
  fun `test getResettlementAssessmentSummaryByNomsId - returns assessment- combination of not started and complete`() {
    val nomsId = "GY3245"
    val assessmentType = ResettlementAssessmentType.BCST2
    val prisonerEntity = PrisonerEntity(1, "GY3245", testDate, "crn", "xyz1", LocalDate.parse("2025-01-23"))
    val accommodationResettlementAssessmentEntity =
      createNotStartedResettlementAssessmentEntity(1, Pathway.ACCOMMODATION)
    val attitudesResettlementAssessmentEntity =
      createNotStartedResettlementAssessmentEntity(2, Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR)
    val childrenFamiliesResettlementAssessmentEntity =
      createCompleteResettlementAssessmentEntity(3, Pathway.CHILDREN_FAMILIES_AND_COMMUNITY)
    val drugsAlcoholResettlementAssessmentEntity =
      createNotStartedResettlementAssessmentEntity(4, Pathway.DRUGS_AND_ALCOHOL)
    val educationSkillsResettlementAssessmentEntity =
      createNotStartedResettlementAssessmentEntity(5, Pathway.EDUCATION_SKILLS_AND_WORK)
    val financeIdResettlementAssessmentEntity = createNotStartedResettlementAssessmentEntity(6, Pathway.FINANCE_AND_ID)
    val healthResettlementAssessmentEntity = createCompleteResettlementAssessmentEntity(7, Pathway.HEALTH)

    val prisonerResettlementAssessmentSummary = listOf(
      PrisonerResettlementAssessment(Pathway.ACCOMMODATION, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(
        Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR,
        ResettlementAssessmentStatus.NOT_STARTED,
      ),
      PrisonerResettlementAssessment(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, ResettlementAssessmentStatus.COMPLETE),
      PrisonerResettlementAssessment(Pathway.DRUGS_AND_ALCOHOL, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.EDUCATION_SKILLS_AND_WORK, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.FINANCE_AND_ID, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.HEALTH, ResettlementAssessmentStatus.COMPLETE),
    )

    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    given(resettlementAssessmentRepository.findLatestForEachPathway(prisonerEntity, assessmentType))
      .willReturn(
        listOf(
          accommodationResettlementAssessmentEntity,
          attitudesResettlementAssessmentEntity,
          childrenFamiliesResettlementAssessmentEntity,
          drugsAlcoholResettlementAssessmentEntity,
          educationSkillsResettlementAssessmentEntity,
          financeIdResettlementAssessmentEntity,
          healthResettlementAssessmentEntity,
        ),
      )
    val response =
      resettlementAssessmentService.getResettlementAssessmentSummaryByNomsId(nomsId, ResettlementAssessmentType.BCST2)
    Assertions.assertEquals(prisonerResettlementAssessmentSummary, response)
  }

  @Test
  fun `test getResettlementAssessmentSummaryByNomsId with BCST2 type - returns assessment with not started statuses for pathways with null value in resettlement_assessment table`() {
    val nomsId: String = "GY3245"
    val assessmentType = ResettlementAssessmentType.BCST2
    val prisonerEntity = PrisonerEntity(1, "GY3245", testDate, "crn", "xyz1", LocalDate.parse("2025-01-23"))
    val accommodationResettlementAssessmentEntity =
      createNotStartedResettlementAssessmentEntity(1, Pathway.ACCOMMODATION)
    val attitudesResettlementAssessmentEntity =
      createNotStartedResettlementAssessmentEntity(2, Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR)
    val drugsAlcoholResettlementAssessmentEntity =
      createNotStartedResettlementAssessmentEntity(4, Pathway.DRUGS_AND_ALCOHOL)
    val educationSkillsResettlementAssessmentEntity =
      createNotStartedResettlementAssessmentEntity(5, Pathway.EDUCATION_SKILLS_AND_WORK)
    val healthResettlementAssessmentEntity = createCompleteResettlementAssessmentEntity(7, Pathway.HEALTH)

    val prisonerResettlementAssessmentSummary = listOf(
      PrisonerResettlementAssessment(Pathway.ACCOMMODATION, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(
        Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR,
        ResettlementAssessmentStatus.NOT_STARTED,
      ),
      PrisonerResettlementAssessment(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.DRUGS_AND_ALCOHOL, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.EDUCATION_SKILLS_AND_WORK, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.FINANCE_AND_ID, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.HEALTH, ResettlementAssessmentStatus.COMPLETE),
    )

    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    given(resettlementAssessmentRepository.findLatestForEachPathway(prisonerEntity, assessmentType))
      .willReturn(
        listOf(
          accommodationResettlementAssessmentEntity,
          attitudesResettlementAssessmentEntity,
          drugsAlcoholResettlementAssessmentEntity,
          educationSkillsResettlementAssessmentEntity,
          healthResettlementAssessmentEntity,
        ),
      )

    val bcst2Response =
      resettlementAssessmentService.getResettlementAssessmentSummaryByNomsId(nomsId, ResettlementAssessmentType.BCST2)
    Assertions.assertEquals(prisonerResettlementAssessmentSummary, bcst2Response)
  }

  @Test
  fun `test getResettlementAssessmentSummaryByNomsId with RESETTLEMENT_PLAN type - returns assessment with not started statuses for pathways with null value in resettlement_assessment`() {
    val nomsId: String = "GY3245"
    val assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN
    val prisonerEntity = PrisonerEntity(1, "GY3245", testDate, "crn", "xyz1", LocalDate.parse("2025-01-23"))
    val accommodationResettlementAssessmentEntity =
      createNotStartedResettlementAssessmentEntity(1, Pathway.ACCOMMODATION)
    val attitudesResettlementAssessmentEntity =
      createNotStartedResettlementAssessmentEntity(2, Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR)
    val drugsAlcoholResettlementAssessmentEntity =
      createNotStartedResettlementAssessmentEntity(4, Pathway.DRUGS_AND_ALCOHOL)
    val educationSkillsResettlementAssessmentEntity =
      createNotStartedResettlementAssessmentEntity(5, Pathway.EDUCATION_SKILLS_AND_WORK)
    val healthResettlementAssessmentEntity = createCompleteResettlementAssessmentEntity(7, Pathway.HEALTH)

    val prisonerResettlementAssessmentSummary = listOf(
      PrisonerResettlementAssessment(Pathway.ACCOMMODATION, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(
        Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR,
        ResettlementAssessmentStatus.NOT_STARTED,
      ),
      PrisonerResettlementAssessment(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.DRUGS_AND_ALCOHOL, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.EDUCATION_SKILLS_AND_WORK, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.FINANCE_AND_ID, ResettlementAssessmentStatus.NOT_STARTED),
      PrisonerResettlementAssessment(Pathway.HEALTH, ResettlementAssessmentStatus.COMPLETE),
    )

    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    given(resettlementAssessmentRepository.findLatestForEachPathway(prisonerEntity, assessmentType))
      .willReturn(
        listOf(
          accommodationResettlementAssessmentEntity,
          attitudesResettlementAssessmentEntity,
          drugsAlcoholResettlementAssessmentEntity,
          educationSkillsResettlementAssessmentEntity,
          healthResettlementAssessmentEntity,
        ),
      )

    val resettlementResponse = resettlementAssessmentService.getResettlementAssessmentSummaryByNomsId(
      nomsId,
      ResettlementAssessmentType.RESETTLEMENT_PLAN,
    )
    Assertions.assertEquals(prisonerResettlementAssessmentSummary, resettlementResponse)
  }

  @ParameterizedTest
  @MethodSource("test convertAnswerToString data")
  fun `test convertAnswerToString`(options: List<Option>?, answer: Answer<*>, expectedString: String) {
    Assertions.assertEquals(expectedString, resettlementAssessmentService.convertAnswerToString(options, answer))
  }

  private fun `test convertAnswerToString data`() = Stream.of(
    Arguments.of(null, StringAnswer("My answer"), "My answer"),
    Arguments.of(null, StringAnswer("My answer"), "My answer"),
    Arguments.of(
      listOf(Option("MY_ANSWER", "My answer"), Option("OTHER_OPTION", "Other option")),
      StringAnswer("MY_ANSWER"),
      "My answer",
    ),
    Arguments.of(null, StringAnswer("MY_ANSWER"), "MY_ANSWER"),
    Arguments.of(
      null,
      MapAnswer(
        listOf(
          mapOf("Address line 1" to "123 Main Street"),
          mapOf("Address line 2" to "Leeds"),
          mapOf("County" to "West Yorkshire"),
          mapOf("Postcode" to "LS1 1AB", "Country" to "United Kingdom"),
        ),
      ),
      "123 Main Street\nLeeds\nWest Yorkshire\nLS1 1AB\nUnited Kingdom",
    ),
    Arguments.of(null, MapAnswer(listOf(mapOf(), mapOf(), mapOf(), mapOf())), ""),
    Arguments.of(null, MapAnswer(listOf()), ""),
    Arguments.of(
      listOf(Option("ANSWER_1", "Answer 1"), Option("ANSWER_2", "Answer 2")),
      ListAnswer(listOf("ANSWER_1", "ANSWER_2", "ANSWER_3")),
      "Answer 1\nAnswer 2\nANSWER_3",
    ),
    Arguments.of(null, ListAnswer(listOf()), ""),
  )

  private fun createNotStartedResettlementAssessmentEntity(id: Long, pathway: Pathway) = ResettlementAssessmentEntity(
    id = id,
    prisoner = PrisonerEntity(1, "GY3245", testDate, "crn", "xyz1", LocalDate.parse("2025-01-23")),
    pathway = pathway,
    assessmentType = ResettlementAssessmentType.BCST2,
    assessmentStatus = ResettlementAssessmentStatus.NOT_STARTED,
    assessment = ResettlementAssessmentQuestionAndAnswerList(mutableListOf()),
    creationDate = fakeNow,
    createdBy = "PO",
    statusChangedTo = Status.NOT_STARTED,
    caseNoteText = "some case note text",
    createdByUserId = "USER_1",
    submissionDate = null,
  )

  private fun createCompleteResettlementAssessmentEntity(id: Long, pathway: Pathway) = ResettlementAssessmentEntity(
    id = id,
    prisoner = PrisonerEntity(1, "GY3245", testDate, "crn", "xyz1", LocalDate.parse("2025-01-23")),
    pathway = pathway,
    assessmentType = ResettlementAssessmentType.BCST2,
    assessmentStatus = ResettlementAssessmentStatus.COMPLETE,
    assessment = ResettlementAssessmentQuestionAndAnswerList(mutableListOf()),
    creationDate = fakeNow,
    createdBy = "PO",
    statusChangedTo = Status.SUPPORT_DECLINED,
    caseNoteText = "some case note text",
    createdByUserId = "USER_1",
    submissionDate = null,
  )

  private fun createSubmittedResettlementAssessmentEntity(pathway: Pathway, user: String, caseNoteText: String) = ResettlementAssessmentEntity(
    id = null,
    prisoner = PrisonerEntity(1, "GY3245", testDate, "crn", "xyz1", LocalDate.parse("2025-01-23")),
    pathway = pathway,
    assessmentType = ResettlementAssessmentType.BCST2,
    assessmentStatus = ResettlementAssessmentStatus.SUBMITTED,
    assessment = ResettlementAssessmentQuestionAndAnswerList(mutableListOf()),
    creationDate = fakeNow,
    createdBy = user,
    statusChangedTo = Status.SUPPORT_DECLINED,
    caseNoteText = caseNoteText,
    createdByUserId = user,
    submissionDate = null,
  )

  private fun getSubmittedResettlementAssessmentEntities(user: String, caseNotePostfix: String) = listOf(
    createSubmittedResettlementAssessmentEntity(Pathway.ACCOMMODATION, user, "${Pathway.ACCOMMODATION.displayName} case note - $caseNotePostfix"),
    createSubmittedResettlementAssessmentEntity(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, user, "${Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR.displayName} case note - $caseNotePostfix"),
    createSubmittedResettlementAssessmentEntity(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, user, "${Pathway.CHILDREN_FAMILIES_AND_COMMUNITY.displayName} case note - $caseNotePostfix"),
    createSubmittedResettlementAssessmentEntity(Pathway.DRUGS_AND_ALCOHOL, user, "${Pathway.DRUGS_AND_ALCOHOL.displayName} case note - $caseNotePostfix"),
    createSubmittedResettlementAssessmentEntity(Pathway.EDUCATION_SKILLS_AND_WORK, user, "${Pathway.EDUCATION_SKILLS_AND_WORK.displayName} case note - $caseNotePostfix"),
    createSubmittedResettlementAssessmentEntity(Pathway.FINANCE_AND_ID, user, "${Pathway.FINANCE_AND_ID.displayName} case note - $caseNotePostfix"),
    createSubmittedResettlementAssessmentEntity(Pathway.HEALTH, user, "${Pathway.HEALTH.displayName} case note - $caseNotePostfix"),
  )

  private fun getExpectedCaseNotesText(pathway: Pathway, caseNotePostfix: String) = "Case note summary from ${pathway.displayName} BCST2 report\n\n${pathway.displayName} case note - $caseNotePostfix"

  @Test
  fun `test processAndGroupAssessmentCaseNotes - short text, no limit chars`() {
    val user = "A user"
    val caseNotePostfix = "short text"
    val assessmentList = getSubmittedResettlementAssessmentEntities(user, caseNotePostfix)

    val expectedUserAndCaseNotes = listOf(
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user, user),
        caseNoteText = "${getExpectedCaseNotesText(Pathway.ACCOMMODATION, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.DRUGS_AND_ALCOHOL, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.EDUCATION_SKILLS_AND_WORK, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.FINANCE_AND_ID, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.HEALTH, caseNotePostfix)}"
      )
    )

    Assertions.assertEquals(expectedUserAndCaseNotes, resettlementAssessmentService.processAndGroupAssessmentCaseNotes(assessmentList, false))
  }

  @Test
  fun `test processAndGroupAssessmentCaseNotes - short text, limit chars`() {
    val user = "A user"
    val caseNotePostfix = "short text"
    val assessmentList = getSubmittedResettlementAssessmentEntities(user, caseNotePostfix)

    val expectedUserAndCaseNotes = listOf(
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user, user),
        caseNoteText = "${getExpectedCaseNotesText(Pathway.ACCOMMODATION, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.DRUGS_AND_ALCOHOL, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.EDUCATION_SKILLS_AND_WORK, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.FINANCE_AND_ID, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.HEALTH, caseNotePostfix)}"
      )
    )

    Assertions.assertEquals(expectedUserAndCaseNotes, resettlementAssessmentService.processAndGroupAssessmentCaseNotes(assessmentList, true))
  }

  @Test
  fun `test processAndGroupAssessmentCaseNotes - long text, no limit chars`() {
    val user = "A user"
    val caseNotePostfix = RandomStringUtils.randomAlphanumeric(1000)
    val assessmentList = getSubmittedResettlementAssessmentEntities(user, caseNotePostfix)

    val expectedUserAndCaseNotes = listOf(
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user, user),
        caseNoteText = "${getExpectedCaseNotesText(Pathway.ACCOMMODATION, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.DRUGS_AND_ALCOHOL, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.EDUCATION_SKILLS_AND_WORK, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.FINANCE_AND_ID, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.HEALTH, caseNotePostfix)}"
      )
    )

    Assertions.assertEquals(expectedUserAndCaseNotes, resettlementAssessmentService.processAndGroupAssessmentCaseNotes(assessmentList, false))
  }

  @Test
  fun `test processAndGroupAssessmentCaseNotes - long text, limit chars`() {
    val user = "A user"
    val caseNotePostfix = RandomStringUtils.randomAlphanumeric(1000)
    val assessmentList = getSubmittedResettlementAssessmentEntities(user, caseNotePostfix)

    val expectedUserAndCaseNotes = listOf(
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user, user),
        caseNoteText = "${getExpectedCaseNotesText(Pathway.ACCOMMODATION, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, caseNotePostfix)}"
      ),
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user, user),
        caseNoteText = "${getExpectedCaseNotesText(Pathway.DRUGS_AND_ALCOHOL, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.EDUCATION_SKILLS_AND_WORK, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.FINANCE_AND_ID, caseNotePostfix)}"
      ),
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user, user),
        caseNoteText = getExpectedCaseNotesText(Pathway.HEALTH, caseNotePostfix)
      ),
    )

    Assertions.assertEquals(expectedUserAndCaseNotes, resettlementAssessmentService.processAndGroupAssessmentCaseNotes(assessmentList, true))
  }

  @Test
  fun `test processAndGroupAssessmentCaseNotes - long text, no limit chars, multiple authors`() {
    val user1 = "A user"
    val user2 = "B user"
    val user3 = "C user"
    val caseNotePostfix = RandomStringUtils.randomAlphanumeric(1000)

    val assessmentList = listOf(
      createSubmittedResettlementAssessmentEntity(Pathway.ACCOMMODATION, user1, "${Pathway.ACCOMMODATION.displayName} case note - $caseNotePostfix"),
      createSubmittedResettlementAssessmentEntity(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, user2, "${Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR.displayName} case note - $caseNotePostfix"),
      createSubmittedResettlementAssessmentEntity(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, user1, "${Pathway.CHILDREN_FAMILIES_AND_COMMUNITY.displayName} case note - $caseNotePostfix"),
      createSubmittedResettlementAssessmentEntity(Pathway.DRUGS_AND_ALCOHOL, user2, "${Pathway.DRUGS_AND_ALCOHOL.displayName} case note - $caseNotePostfix"),
      createSubmittedResettlementAssessmentEntity(Pathway.EDUCATION_SKILLS_AND_WORK, user1, "${Pathway.EDUCATION_SKILLS_AND_WORK.displayName} case note - $caseNotePostfix"),
      createSubmittedResettlementAssessmentEntity(Pathway.FINANCE_AND_ID, user3, "${Pathway.FINANCE_AND_ID.displayName} case note - $caseNotePostfix"),
      createSubmittedResettlementAssessmentEntity(Pathway.HEALTH, user1, "${Pathway.HEALTH.displayName} case note - $caseNotePostfix"),
    )

    val expectedUserAndCaseNotes = listOf(
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user1, user1),
        caseNoteText = "${getExpectedCaseNotesText(Pathway.ACCOMMODATION, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.EDUCATION_SKILLS_AND_WORK, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.HEALTH, caseNotePostfix)}"
      ),
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user2, user2),
        caseNoteText = "${getExpectedCaseNotesText(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.DRUGS_AND_ALCOHOL, caseNotePostfix)}"
      ),
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user3, user3),
        caseNoteText = getExpectedCaseNotesText(Pathway.FINANCE_AND_ID, caseNotePostfix)
      ),
    )

    Assertions.assertEquals(expectedUserAndCaseNotes, resettlementAssessmentService.processAndGroupAssessmentCaseNotes(assessmentList, false))
  }

  @Test
  fun `test processAndGroupAssessmentCaseNotes - long text, limit chars, multiple authors`() {
    val user1 = "A user"
    val user2 = "B user"
    val user3 = "C user"
    val caseNotePostfix = RandomStringUtils.randomAlphanumeric(1000)

    val assessmentList = listOf(
      createSubmittedResettlementAssessmentEntity(Pathway.ACCOMMODATION, user1, "${Pathway.ACCOMMODATION.displayName} case note - $caseNotePostfix"),
      createSubmittedResettlementAssessmentEntity(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, user2, "${Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR.displayName} case note - $caseNotePostfix"),
      createSubmittedResettlementAssessmentEntity(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, user1, "${Pathway.CHILDREN_FAMILIES_AND_COMMUNITY.displayName} case note - $caseNotePostfix"),
      createSubmittedResettlementAssessmentEntity(Pathway.DRUGS_AND_ALCOHOL, user2, "${Pathway.DRUGS_AND_ALCOHOL.displayName} case note - $caseNotePostfix"),
      createSubmittedResettlementAssessmentEntity(Pathway.EDUCATION_SKILLS_AND_WORK, user1, "${Pathway.EDUCATION_SKILLS_AND_WORK.displayName} case note - $caseNotePostfix"),
      createSubmittedResettlementAssessmentEntity(Pathway.FINANCE_AND_ID, user3, "${Pathway.FINANCE_AND_ID.displayName} case note - $caseNotePostfix"),
      createSubmittedResettlementAssessmentEntity(Pathway.HEALTH, user1, "${Pathway.HEALTH.displayName} case note - $caseNotePostfix"),
    )

    val expectedUserAndCaseNotes = listOf(
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user1, user1),
        caseNoteText = "${getExpectedCaseNotesText(Pathway.ACCOMMODATION, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.EDUCATION_SKILLS_AND_WORK, caseNotePostfix)}"
      ),
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user1, user1),
        caseNoteText = getExpectedCaseNotesText(Pathway.HEALTH, caseNotePostfix)
      ),
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user2, user2),
        caseNoteText = "${getExpectedCaseNotesText(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, caseNotePostfix)}\n\n${getExpectedCaseNotesText(Pathway.DRUGS_AND_ALCOHOL, caseNotePostfix)}"
      ),
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user3, user3),
        caseNoteText = getExpectedCaseNotesText(Pathway.FINANCE_AND_ID, caseNotePostfix)
      ),
    )

    Assertions.assertEquals(expectedUserAndCaseNotes, resettlementAssessmentService.processAndGroupAssessmentCaseNotes(assessmentList, true))
  }

  @ParameterizedTest
  @MethodSource("test splitToCharLimit data")
  fun `test splitToCharLimit`(input: List<String>, length: Int, expectedOutput: List<String>) {
    Assertions.assertEquals(expectedOutput, resettlementAssessmentService.splitToCharLimit(input, length))
  }

  private fun `test splitToCharLimit data`() = Stream.of(
    Arguments.of(listOf<String>(), 10, listOf<String>()),
    Arguments.of(listOf<String>(), 10, listOf<String>()),
    Arguments.of(listOf("ninechars"), 10, listOf("ninechars")),
    Arguments.of(listOf("tenchars.."), 10, listOf("tenchars..")),
    Arguments.of(listOf("one", "two"), 10, listOf("one\n\ntwo")),
    Arguments.of(listOf("long text", "something"), 10, listOf("long text", "something")),
    Arguments.of(listOf("text1", "text2", "text3"), 10, listOf("text1\n\ntext2", "text3")),
    Arguments.of(listOf("this", "is a", "sentence", "to", "be split", "up"), 10, listOf("this\n\nis a", "sentence\n\nto", "be split\n\nup")),
    Arguments.of(listOf("this", "is a", "sentence", "to", "be split", "up"), 100, listOf("this\n\nis a\n\nsentence\n\nto\n\nbe split\n\nup")),
  )
}
