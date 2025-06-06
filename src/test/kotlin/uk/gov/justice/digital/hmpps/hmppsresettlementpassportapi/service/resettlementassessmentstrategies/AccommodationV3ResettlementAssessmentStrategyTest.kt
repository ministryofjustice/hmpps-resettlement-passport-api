package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.TagAndQuestionMapping
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentCompleteRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentOption
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Validation
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ValidationType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ProfileTagList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ProfileTagsEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentSimpleQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.getClaimFromJWTToken
import java.time.LocalDateTime
import java.util.stream.Stream
import org.assertj.core.api.Assertions as AssertJAssertions

class AccommodationV3ResettlementAssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.ACCOMMODATION, 3) {

  override fun `test next page function flow - no existing assessment data`(): Stream<Arguments> = Stream.of(
    // Start of flow - send null current page to get first page
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      null,
      "ACCOMMODATION_REPORT",
    ),
    // Any answer to ACCOMMODATION_REPORT, go to SUPPORT_REQUIREMENTS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      "ACCOMMODATION_REPORT",
      "SUPPORT_REQUIREMENTS",
    ),
    // Any answer to SUPPORT_REQUIREMENTS, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      "SUPPORT_REQUIREMENTS",
      "ASSESSMENT_SUMMARY",
    ),
    // Any answer to ASSESSMENT_SUMMARY, go to CHECK_ANSWERS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      "ASSESSMENT_SUMMARY",
      "CHECK_ANSWERS",
    ),
  )

  override fun `test get page from Id - no existing assessment data`(): Stream<Arguments> = Stream.of(
    Arguments.of(
      "ACCOMMODATION_REPORT",
      getExpectedV3AccommodationReportPage(),
    ),
    Arguments.of(
      "SUPPORT_REQUIREMENTS",
      ResettlementAssessmentResponsePage(
        id = "SUPPORT_REQUIREMENTS",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_REQUIREMENTS",
              title = "Support needs",
              subTitle = "Select any needs you have identified that could be met by prison or probation staff.",
              type = TypeOfQuestion.CHECKBOX,
              validation = Validation(type = ValidationType.MANDATORY, message = "Select support needs or select 'No support needs identified'"),
              options = listOf(
                ResettlementAssessmentOption(
                  id = "HELP_TO_FIND_ACCOMMODATION",
                  displayText = "Help to find accommodation",
                  tag = "NO_FIXED_ABODE",
                ),
                ResettlementAssessmentOption(
                  id = "HOME_ADAPTATIONS",
                  displayText = "Home adaptations",
                  tag = "HOME_ADAPTATIONS_POST_RELEASE",
                ),
                ResettlementAssessmentOption(
                  id = "HELP_TO_KEEP_HOME",
                  displayText = "Help to keep their home while in prison",
                  tag = "KEEP_THEIR_HOME",
                ),
                ResettlementAssessmentOption(
                  id = "HOMELESS_APPLICATION",
                  displayText = "Homeless application",
                ),
                ResettlementAssessmentOption(
                  id = "CANCEL_A_TENANCY",
                  displayText = "Cancel a tenancy",
                  tag = "CANCEL_TENANCY",
                ),
                ResettlementAssessmentOption(
                  id = "SET_UP_RENT_ARREARS",
                  displayText = "Set up payment for rent arrears",
                  tag = "PAYMENT_FOR_RENT_ARREARS",
                ),
                ResettlementAssessmentOption(
                  id = "ARRANGE_STORAGE",
                  displayText = "Arrange storage for personal possessions",
                  tag = "ARRANGE_STORAGE_FOR_PERSONAL",
                ),
                ResettlementAssessmentOption(
                  id = "OTHER_SUPPORT_NEEDS",
                  displayText = "Other",
                  freeText = true,
                ),
                ResettlementAssessmentOption(
                  id = "NO_SUPPORT_NEEDS",
                  displayText = "No support needs identified",
                  exclusive = true,
                ),
              ),
            ),
            originalPageId = "SUPPORT_REQUIREMENTS",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_REQUIREMENTS_ADDITIONAL_DETAILS",
              title = "Additional details",
              subTitle = "This information will only be displayed in PSfR.",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "SUPPORT_REQUIREMENTS",
          ),
        ),
      ),
    ),
    Arguments.of(
      "ASSESSMENT_SUMMARY",
      ResettlementAssessmentResponsePage(
        id = "ASSESSMENT_SUMMARY",
        title = "Accommodation report summary",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_NEEDS",
              title = "Accommodation resettlement status",
              subTitle = "Select one option.",
              type = TypeOfQuestion.RADIO,
              validation = Validation(ValidationType.MANDATORY, message = "Select an accommodation resettlement status"),
              options = listOf(
                ResettlementAssessmentOption(
                  id = "SUPPORT_REQUIRED",
                  displayText = "Support required",
                  description = "a need for support has been identified and is accepted",
                ),
                ResettlementAssessmentOption(id = "SUPPORT_NOT_REQUIRED", displayText = "Support not required", description = "no need was identified"),
                ResettlementAssessmentOption(
                  id = "SUPPORT_DECLINED",
                  displayText = "Support declined",
                  description = "a need has been identified but support is declined",
                ),
              ),
            ),
            originalPageId = "ASSESSMENT_SUMMARY",
          ),
        ),
      ),
    ),
    Arguments.of(
      "CHECK_ANSWERS",
      ResettlementAssessmentResponsePage(
        id = "CHECK_ANSWERS",
        questionsAndAnswers = listOf(),
      ),
    ),
  )

  @Test
  fun `test get page from Id - RESETTLEMENT_PLAN with existing BCST2 on same version`() {
    val nomsId = "123"

    val existingAssessment = ResettlementAssessmentQuestionAndAnswerList(
      listOf(
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE", StringAnswer("SOCIAL_HOUSING")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS_SOCIAL_HOUSING", MapAnswer(listOf(mapOf("addressLine1" to "123 main street", "city" to "Leeds", "postcode" to "LS1 123")))),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDITIONAL_DETAILS", StringAnswer("Some additional details")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_WILL_THEY_LIVE", StringAnswer("NO_ANSWER")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_WILL_THEY_LIVE_ADDITIONAL_DETAILS", StringAnswer("Some more additional details")),
        ResettlementAssessmentSimpleQuestionAndAnswer("SUPPORT_REQUIREMENTS", ListAnswer(listOf("HELP_TO_KEEP_HOME", "HOMELESS_APPLICATION"))),
        ResettlementAssessmentSimpleQuestionAndAnswer("SUPPORT_REQUIREMENTS_ADDITIONAL_DETAILS", StringAnswer("Long text field answer")),
        ResettlementAssessmentSimpleQuestionAndAnswer("SUPPORT_NEEDS", StringAnswer("SUPPORT_NOT_REQUIRED")),
      ),
    )

    val resettlementAssessmentEntity = ResettlementAssessmentEntity(1, 1, pathway, Status.IN_PROGRESS, ResettlementAssessmentType.BCST2, existingAssessment, testDate, "", ResettlementAssessmentStatus.SUBMITTED, "some text", "USER_1", submissionDate = null, version = 3, userDeclaration = false)
    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(PrisonerEntity(1, nomsId, testDate, "ABC"))
    whenever(
      resettlementAssessmentRepository.findFirstByPrisonerIdAndPathwayAndAssessmentTypeAndAssessmentStatusInAndDeletedIsFalseOrderByCreationDateDesc(
        1,
        pathway,
        ResettlementAssessmentType.BCST2,
        listOf(ResettlementAssessmentStatus.COMPLETE, ResettlementAssessmentStatus.SUBMITTED),
      ),
    ).thenReturn(resettlementAssessmentEntity)
    whenever(
      resettlementAssessmentRepository.findFirstByPrisonerIdAndPathwayAndAssessmentTypeAndAssessmentStatusInAndDeletedIsFalseOrderByCreationDateDesc(
        1,
        pathway,
        ResettlementAssessmentType.RESETTLEMENT_PLAN,
        listOf(ResettlementAssessmentStatus.COMPLETE, ResettlementAssessmentStatus.SUBMITTED),
      ),
    ).thenReturn(null)

    // Answers from BCST2 should be pre-populated
    val expectedPage = getExpectedV3AccommodationReportPage(
      mapOf(
        "WHERE_DID_THEY_LIVE" to StringAnswer("SOCIAL_HOUSING"),
        "WHERE_DID_THEY_LIVE_ADDRESS_SOCIAL_HOUSING" to MapAnswer(listOf(mapOf("addressLine1" to "123 main street", "city" to "Leeds", "postcode" to "LS1 123"))),
        "WHERE_DID_THEY_LIVE_ADDITIONAL_DETAILS" to StringAnswer("Some additional details"),
        "WHERE_WILL_THEY_LIVE" to StringAnswer("NO_ANSWER"),
        "WHERE_WILL_THEY_LIVE_ADDITIONAL_DETAILS" to StringAnswer("Some more additional details"),
      ),
    )
    val page = resettlementAssessmentStrategy.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.ACCOMMODATION,
      assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN,
      pageId = "ACCOMMODATION_REPORT",
      version = 3,
    )
    Assertions.assertEquals(expectedPage, page)
  }

  @Test
  fun `test get page from Id - RESETTLEMENT_PLAN with existing BCST2 on older version`() {
    val nomsId = "123"

    val existingAssessment = ResettlementAssessmentQuestionAndAnswerList(
      listOf(
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE", StringAnswer("SOCIAL_HOUSING")),
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE_ADDRESS", MapAnswer(listOf(mapOf("addressLine1" to "123 main street", "city" to "Leeds", "postcode" to "LS1 123")))),
        ResettlementAssessmentSimpleQuestionAndAnswer("SUPPORT_NEEDS", StringAnswer("SUPPORT_NOT_REQUIRED")),
      ),
    )

    val resettlementAssessmentEntity = ResettlementAssessmentEntity(1, 1, pathway, Status.NOT_STARTED, ResettlementAssessmentType.BCST2, existingAssessment, testDate, "", ResettlementAssessmentStatus.SUBMITTED, "some text", "USER_1", submissionDate = null, version = 1, userDeclaration = false)
    whenever(prisonerRepository.findByNomsId(nomsId)).thenReturn(PrisonerEntity(1, nomsId, testDate, "ABC"))
    whenever(
      resettlementAssessmentRepository.findFirstByPrisonerIdAndPathwayAndAssessmentTypeAndAssessmentStatusInAndDeletedIsFalseOrderByCreationDateDesc(
        1,
        pathway,
        ResettlementAssessmentType.BCST2,
        listOf(ResettlementAssessmentStatus.COMPLETE, ResettlementAssessmentStatus.SUBMITTED),
      ),
    ).thenReturn(resettlementAssessmentEntity)
    whenever(
      resettlementAssessmentRepository.findFirstByPrisonerIdAndPathwayAndAssessmentTypeAndAssessmentStatusInAndDeletedIsFalseOrderByCreationDateDesc(
        1,
        pathway,
        ResettlementAssessmentType.RESETTLEMENT_PLAN,
        listOf(ResettlementAssessmentStatus.COMPLETE, ResettlementAssessmentStatus.SUBMITTED),
      ),
    ).thenReturn(null)

    // Answers from BCST2 should NOT be pre-populated as the version is different
    val expectedPage = getExpectedV3AccommodationReportPage()
    val page = resettlementAssessmentStrategy.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.ACCOMMODATION,
      assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN,
      pageId = "ACCOMMODATION_REPORT",
      version = 3,
    )
    Assertions.assertEquals(expectedPage, page)
  }

  @Test
  fun `should ignore any question ids that are not in the current config when building check your answers`() {
    val nomsId = "123"

    val existingAssessment = ResettlementAssessmentQuestionAndAnswerList(
      listOf(
        ResettlementAssessmentSimpleQuestionAndAnswer("WHERE_DID_THEY_LIVE", StringAnswer("FAMILY_OR_FRIENDS")),
        ResettlementAssessmentSimpleQuestionAndAnswer("ARE_THEY_A_POTATO", StringAnswer("NO")),
        ResettlementAssessmentSimpleQuestionAndAnswer("DO_THEY_KNOW_HOW_TO_TANGO", StringAnswer("YES")),
      ),
    )

    setUpMocks("123", true, existingAssessment, ResettlementAssessmentStatus.SUBMITTED)

    val page = resettlementAssessmentStrategy.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.ACCOMMODATION,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = "CHECK_ANSWERS",
      version = 3,
    )

    AssertJAssertions.assertThat(page.id).isEqualTo("CHECK_ANSWERS")
    AssertJAssertions.assertThat(page.questionsAndAnswers)
      .hasSize(1)
      .extracting({ it.question.id })
      .contains(Tuple("WHERE_DID_THEY_LIVE"))
  }

  @ParameterizedTest
  @MethodSource("test complete assessment data")
  fun `test complete assessment`(assessmentType: ResettlementAssessmentType, assessment: ResettlementAssessmentCompleteRequest, expectedEntity: ResettlementAssessmentEntity?, expectedException: Throwable?, existingAssessment: ResettlementAssessmentEntity?, expectedProfileTagsEntity: ProfileTagsEntity?) {
    mockkStatic(::getClaimFromJWTToken)
    every { getClaimFromJWTToken("string", "name") } returns "System user"
    every { getClaimFromJWTToken("string", "auth_source") } returns "nomis"
    every { getClaimFromJWTToken("string", "sub") } returns "USER_1"
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns testDate

    val nomsId = "abc"
    val pathway = Pathway.ACCOMMODATION

    val profileTagsList = ProfileTagList(listOf())
    val tagList = mutableListOf<String>()
    profileTagsList.tags = tagList

    val prisonerEntity = PrisonerEntity(1, nomsId, testDate, "ABC")

    val profileTagsEntity = ProfileTagsEntity(1, 1, profileTagsList, LocalDateTime.parse("2023-08-16T12:00"))
    Mockito.lenient().`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)

    Mockito.lenient().`when`(profileTagsRepository.findByPrisonerId(1)).thenReturn(profileTagsEntity)
    Mockito.lenient().`when`(profileTagsRepository.save(any())).thenReturn(expectedProfileTagsEntity)
    if (existingAssessment != null) {
      whenever(
        resettlementAssessmentRepository.findFirstByPrisonerIdAndPathwayAndAssessmentTypeAndAssessmentStatusInAndDeletedIsFalseOrderByCreationDateDesc(
          1,
          Pathway.ACCOMMODATION,
          assessmentType,
          listOf(
            ResettlementAssessmentStatus.COMPLETE,
            ResettlementAssessmentStatus.SUBMITTED,
          ),
        ),
      ).thenReturn(existingAssessment)
    }

    if (expectedException == null) {
      stubSave()
      resettlementAssessmentStrategy.completeAssessment(nomsId, pathway, assessmentType, assessment, "string", true)
      Mockito.verify(resettlementAssessmentRepository).save(expectedEntity!!)
      if (existingAssessment != null) {
        Mockito.lenient().`when`(profileTagsRepository.findByPrisonerId(1)).thenReturn(profileTagsEntity)
        if (existingAssessment.assessmentStatus == ResettlementAssessmentStatus.SUBMITTED) {
          Mockito.verify(profileTagsRepository).save(expectedProfileTagsEntity!!)
        }
      }
    } else {
      val actualException = assertThrows<Throwable> {
        resettlementAssessmentStrategy.completeAssessment(nomsId, pathway, assessmentType, assessment, "string", true)
      }
      Assertions.assertEquals(expectedException::class, actualException::class)
      Assertions.assertEquals(expectedException.message, actualException.message)
    }

    unmockkAll()
  }

  private fun `test complete assessment data`() = Stream.of(
    // Happy path - BCST2 and no existing assessment
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      ResettlementAssessmentCompleteRequest(
        version = 3,
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("PRIVATE_RENTED_HOUSING"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING",
            answer = StringAnswer("RETURN_TO_PREVIOUS_ADDRESS"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE_ADDITIONAL_DETAILS",
            answer = StringAnswer("Some additional details"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("MOVE_TO_NEW_ADDRESS"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS",
            answer = StringAnswer("Some more information here"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_ADDITIONAL_DETAILS",
            answer = StringAnswer("Some other additional details"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_REQUIREMENTS",
            answer = ListAnswer(listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE")),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_REQUIREMENTS_ADDITIONAL_DETAILS",
            answer = StringAnswer("Long text field answer"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_NEEDS",
            answer = StringAnswer("SUPPORT_REQUIRED"),
          ),
        ),
      ),
      ResettlementAssessmentEntity(id = null, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "PRIVATE_RENTED_HOUSING")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING", answer = StringAnswer(answer = "RETURN_TO_PREVIOUS_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Some additional details")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "MOVE_TO_NEW_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS", answer = StringAnswer(answer = "Some more information here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Some other additional details")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_REQUIREMENTS", answer = ListAnswer(answer = listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE"))), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_REQUIREMENTS_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Long text field answer")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_REQUIRED")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = null, createdByUserId = "USER_1", version = 3, submissionDate = null, userDeclaration = true),
      null,
      null,
      null,
    ),
    // Happy path - RESETTLEMENT_PLAN and no existing assessment
    Arguments.of(
      ResettlementAssessmentType.RESETTLEMENT_PLAN,
      ResettlementAssessmentCompleteRequest(
        version = 3,
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("PRIVATE_RENTED_HOUSING"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING",
            answer = StringAnswer("RETURN_TO_PREVIOUS_ADDRESS"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE_ADDITIONAL_DETAILS",
            answer = StringAnswer("Some additional details"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("MOVE_TO_NEW_ADDRESS"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS",
            answer = StringAnswer("Some more information here"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_ADDITIONAL_DETAILS",
            answer = StringAnswer("Some other additional details"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_REQUIREMENTS",
            answer = ListAnswer(listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE")),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_REQUIREMENTS_ADDITIONAL_DETAILS",
            answer = StringAnswer("Long text field answer"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_NEEDS_PRERELEASE",
            answer = StringAnswer("SUPPORT_REQUIRED"),
          ),
        ),
      ),
      ResettlementAssessmentEntity(id = null, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_REQUIRED, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "PRIVATE_RENTED_HOUSING")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING", answer = StringAnswer(answer = "RETURN_TO_PREVIOUS_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Some additional details")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "MOVE_TO_NEW_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS", answer = StringAnswer(answer = "Some more information here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Some other additional details")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_REQUIREMENTS", answer = ListAnswer(answer = listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE"))), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_REQUIREMENTS_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Long text field answer")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS_PRERELEASE", answer = StringAnswer(answer = "SUPPORT_REQUIRED")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = null, createdByUserId = "USER_1", version = 3, submissionDate = null, userDeclaration = true),
      null,
      null,
      null,
    ),
    // Happy path - existing COMPLETE assessment
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      ResettlementAssessmentCompleteRequest(
        version = 3,
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING",
            answer = StringAnswer("RETURN_TO_PREVIOUS_ADDRESS"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE_ADDITIONAL_DETAILS",
            answer = StringAnswer("Some additional details"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("MOVE_TO_NEW_ADDRESS"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS",
            answer = StringAnswer("Some more information here"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_ADDITIONAL_DETAILS",
            answer = StringAnswer("Some other additional details"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_REQUIREMENTS",
            answer = ListAnswer(listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE")),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_REQUIREMENTS_ADDITIONAL_DETAILS",
            answer = StringAnswer("Long text field answer"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_NEEDS",
            answer = StringAnswer("SUPPORT_REQUIRED"),
          ),
        ),
      ),
      ResettlementAssessmentEntity(id = null, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING", answer = StringAnswer(answer = "RETURN_TO_PREVIOUS_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Some additional details")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "MOVE_TO_NEW_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS", answer = StringAnswer(answer = "Some more information here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Some other additional details")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_REQUIREMENTS", answer = ListAnswer(answer = listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE"))), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_REQUIREMENTS_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Long text field answer")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_REQUIRED")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = null, createdByUserId = "USER_1", version = 3, submissionDate = null, userDeclaration = true),
      null,
      ResettlementAssessmentEntity(id = 12, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING", answer = StringAnswer(answer = "RETURN_TO_PREVIOUS_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "MOVE_TO_NEW_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS", answer = StringAnswer(answer = "Some more information here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_REQUIREMENTS", answer = ListAnswer(answer = listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE"))), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_REQUIREMENTS_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Long text field answer")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_REQUIRED")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = null, createdByUserId = "USER_1", version = 3, submissionDate = null, userDeclaration = true),
      null,
    ),
    // Happy path - existing SUBMITTED assessment
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      ResettlementAssessmentCompleteRequest(
        version = 3,
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING",
            answer = StringAnswer("RETURN_TO_PREVIOUS_ADDRESS"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE_ADDITIONAL_DETAILS",
            answer = StringAnswer("Some additional details"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("MOVE_TO_NEW_ADDRESS"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS",
            answer = StringAnswer("Some more information here"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_ADDITIONAL_DETAILS",
            answer = StringAnswer("Some other additional details"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_REQUIREMENTS",
            answer = ListAnswer(listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE")),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_REQUIREMENTS_ADDITIONAL_DETAILS",
            answer = StringAnswer("Long text field answer"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_NEEDS",
            answer = StringAnswer("SUPPORT_REQUIRED"),
          ),
        ),
      ),
      ResettlementAssessmentEntity(id = null, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING", answer = StringAnswer(answer = "RETURN_TO_PREVIOUS_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Some additional details")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "MOVE_TO_NEW_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS", answer = StringAnswer(answer = "Some more information here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Some other additional details")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_REQUIREMENTS", answer = ListAnswer(answer = listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE"))), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_REQUIREMENTS_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Long text field answer")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_REQUIRED")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = null, createdByUserId = "USER_1", version = 3, submissionDate = null, userDeclaration = true),
      null,
      ResettlementAssessmentEntity(id = 12, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING", answer = StringAnswer(answer = "RETURN_TO_PREVIOUS_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "MOVE_TO_NEW_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS", answer = StringAnswer(answer = "Some more information here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_REQUIREMENTS", answer = ListAnswer(answer = listOf("HELP_TO_KEEP_HOME", "ARRANGE_STORAGE"))), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_REQUIREMENTS_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Long text field answer")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_REQUIRED")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = null, createdByUserId = "USER_1", version = 3, submissionDate = null, userDeclaration = true),
      ProfileTagsEntity(id = null, prisonerId = 1, ProfileTagList(listOf(TagAndQuestionMapping.NO_FIXED_ABODE.name)), LocalDateTime.parse("2023-08-16T12:00")),
    ),
    // Happy path - existing SUBMITTED assessment with Tags generated for multiple answers
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      ResettlementAssessmentCompleteRequest(
        version = 3,
        questionsAndAnswers = listOf(
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE",
            answer = StringAnswer("NO_PERMANENT_OR_FIXED"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING",
            answer = StringAnswer("RETURN_TO_PREVIOUS_ADDRESS"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_DID_THEY_LIVE_ADDITIONAL_DETAILS",
            answer = StringAnswer("Some additional details"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE",
            answer = StringAnswer("MOVE_TO_NEW_ADDRESS"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS",
            answer = StringAnswer("Some more information here"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "WHERE_WILL_THEY_LIVE_ADDITIONAL_DETAILS",
            answer = StringAnswer("Some other additional details"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_REQUIREMENTS",
            answer = ListAnswer(listOf("HELP_TO_FIND_ACCOMMODATION", "HOME_ADAPTATIONS", "HELP_TO_KEEP_HOME", "HOMELESS_APPLICATION", "CANCEL_A_TENANCY")),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_REQUIREMENTS_ADDITIONAL_DETAILS",
            answer = StringAnswer("Long text field answer"),
          ),
          ResettlementAssessmentRequestQuestionAndAnswer(
            question = "SUPPORT_NEEDS",
            answer = StringAnswer("SUPPORT_REQUIRED"),
          ),
        ),
      ),
      ResettlementAssessmentEntity(id = null, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING", answer = StringAnswer(answer = "RETURN_TO_PREVIOUS_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Some additional details")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "MOVE_TO_NEW_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS", answer = StringAnswer(answer = "Some more information here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Some other additional details")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_REQUIREMENTS", answer = ListAnswer(answer = listOf("HELP_TO_FIND_ACCOMMODATION", "HOME_ADAPTATIONS", "HELP_TO_KEEP_HOME", "HOMELESS_APPLICATION", "CANCEL_A_TENANCY"))), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_REQUIREMENTS_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Long text field answer")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_REQUIRED")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = null, createdByUserId = "USER_1", version = 3, submissionDate = null, userDeclaration = true),
      null,
      ResettlementAssessmentEntity(id = 12, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = Status.SUPPORT_REQUIRED, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING", answer = StringAnswer(answer = "RETURN_TO_PREVIOUS_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "MOVE_TO_NEW_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS", answer = StringAnswer(answer = "Some more information here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_REQUIREMENTS", answer = ListAnswer(answer = listOf("HELP_TO_KEEP_HOME"))), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_REQUIREMENTS_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Long text field answer")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "SUPPORT_NEEDS", answer = StringAnswer(answer = "SUPPORT_REQUIRED")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = null, createdByUserId = "USER_1", version = 3, submissionDate = null, userDeclaration = true),
      ProfileTagsEntity(id = null, prisonerId = 1, ProfileTagList(listOf(TagAndQuestionMapping.NO_FIXED_ABODE.name, TagAndQuestionMapping.HOME_ADAPTATIONS_POST_RELEASE.name, TagAndQuestionMapping.KEEP_THEIR_HOME.name, TagAndQuestionMapping.CANCEL_TENANCY.name)), LocalDateTime.parse("2023-08-16T12:00")),
    ),
  )
}

fun getExpectedV3AccommodationReportPage(answers: Map<String, Answer<*>>? = null) = ResettlementAssessmentResponsePage(
  id = "ACCOMMODATION_REPORT",
  title = "Accommodation report",
  questionsAndAnswers = listOf(
    ResettlementAssessmentQuestionAndAnswer(
      question = ResettlementAssessmentQuestion(
        id = "WHERE_DID_THEY_LIVE",
        title = "Where did the person in prison live before custody?",
        type = TypeOfQuestion.RADIO,
        validation = Validation(type = ValidationType.MANDATORY, message = "Select where the person in prison lived before custody"),
        options = listOf(
          ResettlementAssessmentOption(
            id = "PRIVATE_RENTED_HOUSING",
            displayText = "Private housing rented by them",
            nestedQuestions = listOf(
              ResettlementAssessmentQuestionAndAnswer(
                question = ResettlementAssessmentQuestion(
                  id = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING",
                  title = "Enter the address",
                  type = TypeOfQuestion.ADDRESS,
                ),
                originalPageId = "ACCOMMODATION_REPORT",
                answer = answers?.get("WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING"),
              ),
            ),
          ),
          ResettlementAssessmentOption(
            id = "PRIVATE_HOUSING_OWNED",
            displayText = "Private housing owned by them",
            nestedQuestions = listOf(
              ResettlementAssessmentQuestionAndAnswer(
                question = ResettlementAssessmentQuestion(
                  id = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_HOUSING_OWNED",
                  title = "Enter the address",
                  type = TypeOfQuestion.ADDRESS,
                ),
                originalPageId = "ACCOMMODATION_REPORT",
                answer = answers?.get("WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_HOUSING_OWNED"),
              ),
            ),
          ),
          ResettlementAssessmentOption(
            id = "FAMILY_OR_FRIENDS",
            displayText = "With family or friends",
            nestedQuestions = listOf(
              ResettlementAssessmentQuestionAndAnswer(
                question = ResettlementAssessmentQuestion(
                  id = "WHERE_DID_THEY_LIVE_ADDRESS_FAMILY_OR_FRIENDS",
                  title = "Enter the address",
                  type = TypeOfQuestion.ADDRESS,
                ),
                originalPageId = "ACCOMMODATION_REPORT",
                answer = answers?.get("WHERE_DID_THEY_LIVE_ADDRESS_FAMILY_OR_FRIENDS"),
              ),
            ),
          ),
          ResettlementAssessmentOption(
            id = "SOCIAL_HOUSING",
            displayText = "Social housing",
            nestedQuestions = listOf(
              ResettlementAssessmentQuestionAndAnswer(
                question = ResettlementAssessmentQuestion(
                  id = "WHERE_DID_THEY_LIVE_ADDRESS_SOCIAL_HOUSING",
                  title = "Enter the address",
                  type = TypeOfQuestion.ADDRESS,
                ),
                originalPageId = "ACCOMMODATION_REPORT",
                answer = answers?.get("WHERE_DID_THEY_LIVE_ADDRESS_SOCIAL_HOUSING"),
              ),
            ),
          ),
          ResettlementAssessmentOption(
            id = "LOCAL_AUTHORITY_OR_SUPPORTED_HOUSING",
            displayText = "Local authority care or supported housing",
            nestedQuestions = listOf(
              ResettlementAssessmentQuestionAndAnswer(
                question = ResettlementAssessmentQuestion(
                  id = "WHERE_DID_THEY_LIVE_ADDRESS_LOCAL_AUTHORITY_OR_SUPPORTED_HOUSING",
                  title = "Enter the address",
                  type = TypeOfQuestion.ADDRESS,
                ),
                originalPageId = "ACCOMMODATION_REPORT",
                answer = answers?.get("WHERE_DID_THEY_LIVE_ADDRESS_LOCAL_AUTHORITY_OR_SUPPORTED_HOUSING"),
              ),
            ),
          ),
          ResettlementAssessmentOption(
            id = "HOSTEL",
            displayText = "Hostel",
            nestedQuestions = listOf(
              ResettlementAssessmentQuestionAndAnswer(
                question = ResettlementAssessmentQuestion(
                  id = "WHERE_DID_THEY_LIVE_ADDRESS_HOSTEL",
                  title = "Enter the address",
                  type = TypeOfQuestion.ADDRESS,
                ),
                originalPageId = "ACCOMMODATION_REPORT",
                answer = answers?.get("WHERE_DID_THEY_LIVE_ADDRESS_HOSTEL"),
              ),
            ),
          ),
          ResettlementAssessmentOption(
            id = "EMERGENCY_HOUSING",
            displayText = "Emergency housing from the council",
            nestedQuestions = listOf(
              ResettlementAssessmentQuestionAndAnswer(
                question = ResettlementAssessmentQuestion(
                  id = "WHERE_DID_THEY_LIVE_ADDRESS_EMERGENCY_HOUSING",
                  title = "Enter the address",
                  type = TypeOfQuestion.ADDRESS,
                ),
                originalPageId = "ACCOMMODATION_REPORT",
                answer = answers?.get("WHERE_DID_THEY_LIVE_ADDRESS_EMERGENCY_HOUSING"),
              ),
            ),
          ),
          ResettlementAssessmentOption(
            id = "APPROVED_PREMISES",
            displayText = "Community accommodation, including approved premises, CAS2 and CAS3",
            nestedQuestions = listOf(
              ResettlementAssessmentQuestionAndAnswer(
                question = ResettlementAssessmentQuestion(
                  id = "WHERE_DID_THEY_LIVE_ADDRESS_APPROVED_PREMISES",
                  title = "Enter the address",
                  type = TypeOfQuestion.ADDRESS,
                ),
                originalPageId = "ACCOMMODATION_REPORT",
                answer = answers?.get("WHERE_DID_THEY_LIVE_ADDRESS_APPROVED_PREMISES"),
              ),
            ),
          ),
          ResettlementAssessmentOption(
            id = "IMMIGRATION_ACCOMMODATION",
            displayText = "Immigration accommodation provided by the Home Office",
            nestedQuestions = listOf(
              ResettlementAssessmentQuestionAndAnswer(
                question = ResettlementAssessmentQuestion(
                  id = "WHERE_DID_THEY_LIVE_ADDRESS_IMMIGRATION_ACCOMMODATION",
                  title = "Enter the address",
                  type = TypeOfQuestion.ADDRESS,
                ),
                originalPageId = "ACCOMMODATION_REPORT",
                answer = answers?.get("WHERE_DID_THEY_LIVE_ADDRESS_IMMIGRATION_ACCOMMODATION"),
              ),
            ),
          ),
          ResettlementAssessmentOption(
            id = "NO_PERMANENT_OR_FIXED",
            displayText = "No permanent or fixed address",
          ),
          ResettlementAssessmentOption(
            id = "NO_ANSWER",
            displayText = "No answer provided",
          ),
        ),
      ),
      originalPageId = "ACCOMMODATION_REPORT",
      answer = answers?.get("WHERE_DID_THEY_LIVE"),
    ),
    ResettlementAssessmentQuestionAndAnswer(
      question = ResettlementAssessmentQuestion(
        id = "WHERE_DID_THEY_LIVE_ADDITIONAL_DETAILS",
        title = "Additional details",
        subTitle = "Include the name and date of birth of anyone else who lived at the address, and how the accommodation was paid for.\nIf no fixed address, specify the council area where they have a local connection.",
        type = TypeOfQuestion.LONG_TEXT,
        validationType = ValidationType.OPTIONAL,
        validation = Validation(ValidationType.OPTIONAL),
      ),
      originalPageId = "ACCOMMODATION_REPORT",
      answer = answers?.get("WHERE_DID_THEY_LIVE_ADDITIONAL_DETAILS"),
    ),
    ResettlementAssessmentQuestionAndAnswer(
      question = ResettlementAssessmentQuestion(
        id = "WHERE_WILL_THEY_LIVE",
        title = "Where will the person in prison live when they are released?",
        type = TypeOfQuestion.RADIO,
        validation = Validation(type = ValidationType.MANDATORY, message = "Select where the person in prison will live when they are released"),
        options = listOf(
          ResettlementAssessmentOption(
            id = "RETURN_TO_PREVIOUS_ADDRESS",
            displayText = "Return to their previous address",
          ),
          ResettlementAssessmentOption(
            id = "MOVE_TO_NEW_ADDRESS",
            displayText = "Move to a new address",
            nestedQuestions = listOf(
              ResettlementAssessmentQuestionAndAnswer(
                question = ResettlementAssessmentQuestion(
                  id = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS",
                  title = "Enter the address",
                  type = TypeOfQuestion.ADDRESS,
                ),
                originalPageId = "ACCOMMODATION_REPORT",
                answer = answers?.get("WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS"),
              ),
            ),
          ),
          ResettlementAssessmentOption(
            id = "DOES_NOT_HAVE_ANYWHERE",
            displayText = "Does not have anywhere to live",
          ),
          ResettlementAssessmentOption(
            id = "NO_ANSWER",
            displayText = "No answer provided",
          ),
        ),
      ),
      originalPageId = "ACCOMMODATION_REPORT",
      answer = answers?.get("WHERE_WILL_THEY_LIVE"),
    ),
    ResettlementAssessmentQuestionAndAnswer(
      question = ResettlementAssessmentQuestion(
        id = "WHERE_WILL_THEY_LIVE_ADDITIONAL_DETAILS",
        title = "Additional details",
        subTitle = "If returning to previous address, specify if any details will have changed.\nIf moving to a new address, include names and dates of birth of anyone else living at the address, and how the accommodation will be paid for.",
        type = TypeOfQuestion.LONG_TEXT,
        validationType = ValidationType.OPTIONAL,
        validation = Validation(ValidationType.OPTIONAL),
      ),
      originalPageId = "ACCOMMODATION_REPORT",
      answer = answers?.get("WHERE_WILL_THEY_LIVE_ADDITIONAL_DETAILS"),
    ),
  ),
)
