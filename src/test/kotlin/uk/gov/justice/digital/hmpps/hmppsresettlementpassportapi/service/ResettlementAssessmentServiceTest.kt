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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.DeliusCaseNoteType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.TagAndQuestionMapping
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.PrisonerResettlementAssessment
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentOption
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentSimpleQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.AssessmentSkipRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.CaseNoteRetryRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ProfileTagsRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.external.ResettlementPassportDeliusApiService
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies.processProfileTags
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
  private lateinit var prisonerSearchApiService: PrisonerSearchApiService

  @Mock
  private lateinit var resettlementPassportDeliusApiService: ResettlementPassportDeliusApiService

  @Mock
  private lateinit var caseNoteRetryRepository: CaseNoteRetryRepository

  @Mock
  private lateinit var profileTagsRepository: ProfileTagsRepository

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
      prisonerSearchApiService,
      resettlementPassportDeliusApiService,
      caseNoteRetryRepository,
      profileTagsRepository,
      "https://resettlement-passport-ui-dev.hmpps.service.justice.gov.uk",
    )
  }

  @Test
  fun `processAndGroupAssessmentCaseNotes - should add description to Delius case notes when there are multiple case notes`() {
    val user1 = "User one"
    val user2 = "User two"
    val caseNotePostfix = RandomStringUtils.randomAlphanumeric(100)

    val assessmentList = listOf(
      createSubmittedResettlementAssessmentEntity(Pathway.ACCOMMODATION, user1, "${Pathway.ACCOMMODATION.displayName} case note - $caseNotePostfix"),
      createSubmittedResettlementAssessmentEntity(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, user2, "${Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR.displayName} case note - $caseNotePostfix"),
      createSubmittedResettlementAssessmentEntity(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, user1, "${Pathway.CHILDREN_FAMILIES_AND_COMMUNITY.displayName} case note - $caseNotePostfix"),
    )

    // immediate needs report
    val expectedUserAndCaseNotes = listOf(
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user1, user1),
        caseNoteText = "Part 1 of 2\n\n${getExpectedCaseNotesText(Pathway.ACCOMMODATION, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, caseNotePostfix)}",
        description = "NOMIS - Immediate needs report - Part 1 of 2",
        deliusCaseNoteType = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      ),
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user2, user2),
        caseNoteText = "Part 2 of 2\n\n${getExpectedCaseNotesText(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, caseNotePostfix)}",
        description = "NOMIS - Immediate needs report - Part 2 of 2",
        deliusCaseNoteType = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      ),
    )

    val immediateNeedsProcessedCaseNotes = resettlementAssessmentService.processAndGroupAssessmentCaseNotes(assessmentList, false, ResettlementAssessmentType.BCST2)

    Assertions.assertEquals(expectedUserAndCaseNotes.size, immediateNeedsProcessedCaseNotes.size)
    for (i in expectedUserAndCaseNotes.indices) {
      Assertions.assertEquals(expectedUserAndCaseNotes[i].user, immediateNeedsProcessedCaseNotes[i].user)
      Assertions.assertEquals(expectedUserAndCaseNotes[i].caseNoteText, immediateNeedsProcessedCaseNotes[i].caseNoteText)
      Assertions.assertEquals(expectedUserAndCaseNotes[i].description, immediateNeedsProcessedCaseNotes[i].description)
    }

    // pre-release report
    val expectedUserAndCaseNotesPreRelease = listOf(
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user1, user1),
        caseNoteText = "Part 1 of 2\n\n${getExpectedCaseNotesText(Pathway.ACCOMMODATION, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, caseNotePostfix)}",
        description = "NOMIS - Pre-release report - Part 1 of 2",
        deliusCaseNoteType = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      ),
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user2, user2),
        caseNoteText = "Part 2 of 2\n\n${getExpectedCaseNotesText(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, caseNotePostfix)}",
        description = "NOMIS - Pre-release report - Part 2 of 2",
        deliusCaseNoteType = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      ),
    )

    val preReleaseProcessedCaseNotes = resettlementAssessmentService.processAndGroupAssessmentCaseNotes(assessmentList, false, ResettlementAssessmentType.RESETTLEMENT_PLAN)

    Assertions.assertEquals(expectedUserAndCaseNotesPreRelease.size, preReleaseProcessedCaseNotes.size)
    for (i in expectedUserAndCaseNotesPreRelease.indices) {
      Assertions.assertEquals(expectedUserAndCaseNotesPreRelease[i].user, preReleaseProcessedCaseNotes[i].user)
      Assertions.assertEquals(expectedUserAndCaseNotesPreRelease[i].caseNoteText, preReleaseProcessedCaseNotes[i].caseNoteText)
      Assertions.assertEquals(expectedUserAndCaseNotesPreRelease[i].description, preReleaseProcessedCaseNotes[i].description)
    }
  }

  @Test
  fun `processAndGroupAssessmentCaseNotes - should not add description for single case note`() {
    val user = "Single user"
    val caseNotePostfix = RandomStringUtils.randomAlphanumeric(100)

    val assessmentList = listOf(
      createSubmittedResettlementAssessmentEntity(Pathway.ACCOMMODATION, user, "${Pathway.ACCOMMODATION.displayName} case note - $caseNotePostfix"),
    )

    val expectedUserAndCaseNote = ResettlementAssessmentService.UserAndCaseNote(
      user = ResettlementAssessmentService.User(user, user),
      caseNoteText = getExpectedCaseNotesText(Pathway.ACCOMMODATION, caseNotePostfix),
      deliusCaseNoteType = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      description = null,
    )

    val processedCaseNotes = resettlementAssessmentService.processAndGroupAssessmentCaseNotes(assessmentList, false, ResettlementAssessmentType.BCST2)

    Assertions.assertEquals(1, processedCaseNotes.size)
    Assertions.assertEquals(expectedUserAndCaseNote.user, processedCaseNotes[0].user)
    Assertions.assertEquals(expectedUserAndCaseNote.caseNoteText, processedCaseNotes[0].caseNoteText)
    Assertions.assertEquals(expectedUserAndCaseNote.description, processedCaseNotes[0].description)
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
    given(resettlementAssessmentRepository.findLatestForEachPathway(prisonerEntity.id(), assessmentType))
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
    val nomsId = "GY3245"
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
    given(resettlementAssessmentRepository.findLatestForEachPathway(prisonerEntity.id(), assessmentType))
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
    val nomsId = "GY3245"
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
    given(resettlementAssessmentRepository.findLatestForEachPathway(prisonerEntity.id(), assessmentType))
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
  fun `test convertAnswerToString`(options: List<ResettlementAssessmentOption>?, answer: Answer<*>, expectedString: String) {
    Assertions.assertEquals(expectedString, resettlementAssessmentService.convertAnswerToString(options, answer))
  }

  private fun `test convertAnswerToString data`() = Stream.of(
    Arguments.of(null, StringAnswer("My answer"), "My answer"),
    Arguments.of(null, StringAnswer("My answer"), "My answer"),
    Arguments.of(
      listOf(ResettlementAssessmentOption("MY_ANSWER", "My answer"), ResettlementAssessmentOption("OTHER_OPTION", "Other option")),
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
      listOf(ResettlementAssessmentOption("ANSWER_1", "Answer 1"), ResettlementAssessmentOption("ANSWER_2", "Answer 2")),
      ListAnswer(listOf("ANSWER_1", "ANSWER_2", "ANSWER_3")),
      "Answer 1\nAnswer 2\nANSWER_3",
    ),
    Arguments.of(null, ListAnswer(listOf()), ""),
  )

  private fun createNotStartedResettlementAssessmentEntity(id: Long, pathway: Pathway) = ResettlementAssessmentEntity(
    id = id,
    prisonerId = 1,
    pathway = pathway,
    assessmentType = ResettlementAssessmentType.BCST2,
    assessmentStatus = ResettlementAssessmentStatus.NOT_STARTED,
    assessment = ResettlementAssessmentQuestionAndAnswerList(listOf()),
    creationDate = fakeNow,
    createdBy = "PO",
    statusChangedTo = Status.NOT_STARTED,
    caseNoteText = "some case note text",
    createdByUserId = "USER_1",
    submissionDate = null,
    version = 1,
  )

  private fun createCompleteResettlementAssessmentEntity(id: Long, pathway: Pathway) = ResettlementAssessmentEntity(
    id = id,
    prisonerId = 1,
    pathway = pathway,
    assessmentType = ResettlementAssessmentType.BCST2,
    assessmentStatus = ResettlementAssessmentStatus.COMPLETE,
    assessment = ResettlementAssessmentQuestionAndAnswerList(listOf()),
    creationDate = fakeNow,
    createdBy = "PO",
    statusChangedTo = Status.SUPPORT_DECLINED,
    caseNoteText = "some case note text",
    createdByUserId = "USER_1",
    submissionDate = null,
    version = 1,
  )

  private fun createSubmittedResettlementAssessmentEntity(pathway: Pathway, user: String, caseNoteText: String) = ResettlementAssessmentEntity(
    id = null,
    prisonerId = 1,
    pathway = pathway,
    assessmentType = ResettlementAssessmentType.BCST2,
    assessmentStatus = ResettlementAssessmentStatus.SUBMITTED,
    assessment = ResettlementAssessmentQuestionAndAnswerList(listOf()),
    creationDate = fakeNow,
    createdBy = user,
    statusChangedTo = Status.SUPPORT_DECLINED,
    caseNoteText = caseNoteText,
    createdByUserId = user,
    submissionDate = null,
    version = 1,
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

  private fun getExpectedCaseNotesText(pathway: Pathway, caseNotePostfix: String) = "${pathway.displayName}\n\n${pathway.displayName} case note - $caseNotePostfix"

  @Test
  fun `test processAndGroupAssessmentCaseNotes - short text, no limit chars`() {
    val user = "A user"
    val caseNotePostfix = "short text"
    val assessmentList = getSubmittedResettlementAssessmentEntities(user, caseNotePostfix)

    val expectedUserAndCaseNotes = listOf(
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user, user),
        caseNoteText = "${getExpectedCaseNotesText(Pathway.ACCOMMODATION, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.DRUGS_AND_ALCOHOL, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.EDUCATION_SKILLS_AND_WORK, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.FINANCE_AND_ID, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.HEALTH, caseNotePostfix)}",
        deliusCaseNoteType = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
        description = null,
      ),
    )

    Assertions.assertEquals(expectedUserAndCaseNotes, resettlementAssessmentService.processAndGroupAssessmentCaseNotes(assessmentList, false, ResettlementAssessmentType.BCST2))
  }

  @Test
  fun `test processAndGroupAssessmentCaseNotes - short text, limit chars`() {
    val user = "A user"
    val caseNotePostfix = "short text"
    val assessmentList = getSubmittedResettlementAssessmentEntities(user, caseNotePostfix)

    val expectedUserAndCaseNotes = listOf(
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user, user),
        caseNoteText = "${getExpectedCaseNotesText(Pathway.ACCOMMODATION, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.DRUGS_AND_ALCOHOL, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.EDUCATION_SKILLS_AND_WORK, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.FINANCE_AND_ID, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.HEALTH, caseNotePostfix)}",
        deliusCaseNoteType = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
        description = null,
      ),
    )

    Assertions.assertEquals(expectedUserAndCaseNotes, resettlementAssessmentService.processAndGroupAssessmentCaseNotes(assessmentList, true, ResettlementAssessmentType.BCST2))
  }

  @Test
  fun `test processAndGroupAssessmentCaseNotes - long text, no limit chars`() {
    val user = "A user"
    val caseNotePostfix = RandomStringUtils.randomAlphanumeric(1000)
    val assessmentList = getSubmittedResettlementAssessmentEntities(user, caseNotePostfix)

    val expectedUserAndCaseNotes = listOf(
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user, user),
        caseNoteText = "${getExpectedCaseNotesText(Pathway.ACCOMMODATION, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.DRUGS_AND_ALCOHOL, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.EDUCATION_SKILLS_AND_WORK, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.FINANCE_AND_ID, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.HEALTH, caseNotePostfix)}",
        deliusCaseNoteType = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
        description = null,

      ),
    )

    Assertions.assertEquals(expectedUserAndCaseNotes, resettlementAssessmentService.processAndGroupAssessmentCaseNotes(assessmentList, false, ResettlementAssessmentType.BCST2))
  }

  @Test
  fun `test processAndGroupAssessmentCaseNotes - long text, limit chars`() {
    val user = "A user"
    val caseNotePostfix = RandomStringUtils.randomAlphanumeric(1000)
    val assessmentList = getSubmittedResettlementAssessmentEntities(user, caseNotePostfix)

    val expectedUserAndCaseNotes = listOf(
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user, user),
        caseNoteText = "Part 1 of 3\n\n${getExpectedCaseNotesText(Pathway.ACCOMMODATION, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, caseNotePostfix)}",
        description = "NOMIS - Immediate needs report - Part 1 of 3",
        deliusCaseNoteType = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      ),
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user, user),
        caseNoteText = "Part 2 of 3\n\n${getExpectedCaseNotesText(Pathway.DRUGS_AND_ALCOHOL, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.EDUCATION_SKILLS_AND_WORK, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.FINANCE_AND_ID, caseNotePostfix)}",
        description = "NOMIS - Immediate needs report - Part 2 of 3",
        deliusCaseNoteType = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      ),
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user, user),
        caseNoteText = "Part 3 of 3\n\n${getExpectedCaseNotesText(Pathway.HEALTH, caseNotePostfix)}",
        description = "NOMIS - Immediate needs report - Part 3 of 3",
        deliusCaseNoteType = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      ),
    )

    Assertions.assertEquals(expectedUserAndCaseNotes, resettlementAssessmentService.processAndGroupAssessmentCaseNotes(assessmentList, true, ResettlementAssessmentType.BCST2))
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
        caseNoteText = "Part 1 of 3\n\n${getExpectedCaseNotesText(Pathway.ACCOMMODATION, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.EDUCATION_SKILLS_AND_WORK, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.HEALTH, caseNotePostfix)}",
        description = "NOMIS - Immediate needs report - Part 1 of 3",
        deliusCaseNoteType = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      ),
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user2, user2),
        caseNoteText = "Part 2 of 3\n\n${getExpectedCaseNotesText(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.DRUGS_AND_ALCOHOL, caseNotePostfix)}",
        description = "NOMIS - Immediate needs report - Part 2 of 3",
        deliusCaseNoteType = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      ),
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user3, user3),
        caseNoteText = "Part 3 of 3\n\n${getExpectedCaseNotesText(Pathway.FINANCE_AND_ID, caseNotePostfix)}",
        description = "NOMIS - Immediate needs report - Part 3 of 3",
        deliusCaseNoteType = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      ),
    )

    Assertions.assertEquals(expectedUserAndCaseNotes, resettlementAssessmentService.processAndGroupAssessmentCaseNotes(assessmentList, false, ResettlementAssessmentType.BCST2))
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
        caseNoteText = "Part 1 of 4\n\n${getExpectedCaseNotesText(Pathway.ACCOMMODATION, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.EDUCATION_SKILLS_AND_WORK, caseNotePostfix)}",
        description = "NOMIS - Immediate needs report - Part 1 of 4",
        deliusCaseNoteType = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      ),
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user1, user1),
        caseNoteText = "Part 2 of 4\n\n${getExpectedCaseNotesText(Pathway.HEALTH, caseNotePostfix)}",
        description = "NOMIS - Immediate needs report - Part 2 of 4",
        deliusCaseNoteType = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      ),
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user2, user2),
        caseNoteText = "Part 3 of 4\n\n${getExpectedCaseNotesText(Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, caseNotePostfix)}\n\n\n${getExpectedCaseNotesText(Pathway.DRUGS_AND_ALCOHOL, caseNotePostfix)}",
        description = "NOMIS - Immediate needs report - Part 3 of 4",
        deliusCaseNoteType = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      ),
      ResettlementAssessmentService.UserAndCaseNote(
        user = ResettlementAssessmentService.User(user3, user3),
        caseNoteText = "Part 4 of 4\n\n${getExpectedCaseNotesText(Pathway.FINANCE_AND_ID, caseNotePostfix)}",
        description = "NOMIS - Immediate needs report - Part 4 of 4",
        deliusCaseNoteType = DeliusCaseNoteType.IMMEDIATE_NEEDS_REPORT,
      ),
    )

    Assertions.assertEquals(expectedUserAndCaseNotes, resettlementAssessmentService.processAndGroupAssessmentCaseNotes(assessmentList, true, ResettlementAssessmentType.BCST2))
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
    Arguments.of(listOf("one", "two"), 10, listOf("one\n\n\ntwo")),
    Arguments.of(listOf("long text", "something"), 10, listOf("long text", "something")),
    Arguments.of(listOf("text1", "text2", "text3"), 10, listOf("text1\n\n\ntext2", "text3")),
    Arguments.of(listOf("this", "is a", "sentence", "to", "be split", "up"), 10, listOf("this\n\n\nis a", "sentence\n\n\nto", "be split\n\n\nup")),
    Arguments.of(listOf("this", "is a", "sentence", "to", "be split", "up"), 100, listOf("this\n\n\nis a\n\n\nsentence\n\n\nto\n\n\nbe split\n\n\nup")),
  )

  @Test
  fun `test processProfileTags - create profile tag`() {
    val user = "A user"
    val caseNotePostfix = "short text"

    val questions = emptyList<ResettlementAssessmentSimpleQuestionAndAnswer>()
    val assessment = ResettlementAssessmentQuestionAndAnswerList(listOf())
    val assessmentQA1 = ResettlementAssessmentSimpleQuestionAndAnswer("HELP_TO_FIND_ACCOMMODATION", StringAnswer("YES"))
    val assessmentQA2 = ResettlementAssessmentSimpleQuestionAndAnswer("HOME_ADAPTATIONS", StringAnswer("YES"))
    assessment.assessment.toMutableList().add(assessmentQA1)
    assessment.assessment.toMutableList().add(assessmentQA2)
    val resettlementAssessmentEntity = createSubmittedResettlementAssessmentEntity(Pathway.ACCOMMODATION, user, "${Pathway.ACCOMMODATION.displayName} case note - $caseNotePostfix")
    resettlementAssessmentEntity.assessment.assessment = assessment.assessment
    val profileTagList = processProfileTags(resettlementAssessmentEntity, Pathway.ACCOMMODATION)
    val expectedTagAndQuestionMappingList = emptyList<TagAndQuestionMapping>()
    expectedTagAndQuestionMappingList.toMutableList().add(TagAndQuestionMapping.NO_FIXED_ABODE)
    expectedTagAndQuestionMappingList.toMutableList().add(TagAndQuestionMapping.HOME_ADAPTION_POST_RELEASE)

    Assertions.assertEquals(expectedTagAndQuestionMappingList, profileTagList)
  }
}
