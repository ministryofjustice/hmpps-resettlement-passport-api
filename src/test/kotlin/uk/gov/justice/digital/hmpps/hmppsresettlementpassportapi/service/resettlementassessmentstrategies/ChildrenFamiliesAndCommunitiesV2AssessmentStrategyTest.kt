package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.CustomValidation
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentCompleteRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentOption
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ValidationType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.yesNoOptions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentSimpleQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.getClaimFromJWTToken
import java.time.LocalDateTime
import java.util.stream.Stream

class ChildrenFamiliesAndCommunitiesV2AssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, 2) {

  @ParameterizedTest(name = "{1} -> {2}")
  @MethodSource("test next page function flow - no existing assessment data")
  fun `test next page function flow - no existing assessment`(
    questionsAndAnswers: List<ResettlementAssessmentRequestQuestionAndAnswer<*>>,
    currentPage: String?,
    expectedPage: String,
  ) {
    val nomsId = "123"
    setUpMocks(nomsId, false)

    val assessment = ResettlementAssessmentRequest(
      questionsAndAnswers = questionsAndAnswers,
    )
    val nextPage = resettlementAssessmentStrategy.getNextPageId(
      assessment = assessment,
      nomsId = nomsId,
      pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY,
      assessmentType = ResettlementAssessmentType.BCST2,
      currentPage = currentPage,
      version = 2,
    )
    Assertions.assertEquals(expectedPage, nextPage)
  }

  private fun `test next page function flow - no existing assessment data`() = Stream.of(
    // Start of flow - send null current page to get first page
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      null,
      "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
    ),
    // Any answer to CHILDREN_FAMILIES_AND_COMMUNITY_REPORT, go to SUPPORT_REQUIREMENTS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
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

  @ParameterizedTest(name = "{0} page")
  @MethodSource("test get page from Id - no existing assessment data")
  fun `test get page from Id - no existing assessment`(pageIdInput: String, expectedPage: ResettlementAssessmentResponsePage) {
    val nomsId = "123"
    setUpMocks("123", false)

    val page = resettlementAssessmentStrategy.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = pageIdInput,
      version = 2,
    )
    Assertions.assertEquals(expectedPage, page)
  }

  private fun `test get page from Id - no existing assessment data`() = Stream.of(
    Arguments.of(
      "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
      ResettlementAssessmentResponsePage(
        id = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
        title = "Children, families and communities report",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "PARTNER_OR_SPOUSE",
              title = "Does the person in prison have a partner or spouse?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "PARENTAL_RESPONSIBILITY",
              title = "Does the person in prison have parental responsibility for any children under 16?",
              subTitle = "Parental responsibility means they have legal rights and duties relating to the children's upbringing. It does not mean they are allowed contact.",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "PRIMARY_CARER_FOR_CHILDREN",
              title = "Is the person in prison the primary carer for any children under 16?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = listOf(
                ResettlementAssessmentOption(
                  id = "YES",
                  displayText = "Yes",
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "NUMBER_OF_CHILDREN",
                        title = "Number of children",
                        type = TypeOfQuestion.SHORT_TEXT,
                        customValidation = CustomValidation(regex = "^(?:[1-9])(\\d+)?$", message = "Number of children must be a whole number"),
                      ),
                      originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
                    ),
                  ),
                ),
                ResettlementAssessmentOption(
                  id = "NO",
                  displayText = "No",
                ),
                ResettlementAssessmentOption(
                  id = "NO_ANSWER",
                  displayText = "No answer provided",
                ),
              ),
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "CHILDREN_SERVICE_INVOLVED",
              title = "Are children's services involved with the person in prison and the children they look after?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "CARING_RESPONSIBILITIES_FOR_ADULTS",
              title = "Does the person in prison have caring responsibilities for any adults?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SOCIAL_SERVICE_INVOLVED",
              title = "Are social services involved with the person in prison and any adults they provide care for?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "RECEIVED_SUPPORT_FROM_SOCIAL_SERVICES",
              title = "Has the person in prison ever received support from social services or been in care?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "DO_THEY_HAVE_SUPPORT_FROM_FAMILY_FRIENDS_COMMUNITY",
              title = "Does the person in prison have support from family, friends or their community outside of prison?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "INVOLVEMENT_IN_GANG_ACTIVITY",
              title = "Has the person in prison had any involvement in gang activity?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "UNDER_THREAT_OUTSIDE_PRISON",
              title = "Is the person in prison under threat outside of prison?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "VICTIM_OF_DOMESTIC_ABUSE",
              title = "Has the person in prison ever been the victim of domestic abuse?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "PERPETRATOR_OF_DOMESTIC_ABUSE",
              title = "Has the person in prison ever been the perpetrator of domestic abuse?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "VICTIM_OF_SEXUAL_ABUSE",
              title = "Has the person in prison ever been the victim of sexual abuse?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "PERPETRATOR_OF_SEXUAL_ABUSE",
              title = "Has the person in prison ever been the perpetrator of sexual abuse?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "WORKED_IN_SEX_INDUSTRY",
              title = "Has the person in prison ever worked in the sex industry?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
        ),
      ),
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
              options = listOf(
                ResettlementAssessmentOption(
                  id = "SUPPORT_WHEN_MEETING_CHILDREN_SERVICES",
                  displayText = "Support when they meet with children's services",
                  tag = "MEET_CHILDREN",
                ),
                ResettlementAssessmentOption(
                  id = "SUPPORT_FROM_COMMUNITY_ORGANISATION_OUTSIDE_PRISON",
                  displayText = "Support from community organisations outside of prison",
                  tag = "COMMUNITY_ORG_SUPPORT",
                ),
                ResettlementAssessmentOption(
                  id = "INTERNAL_SUPPORT_SERVICES",
                  displayText = "Internal support services",
                ),
                ResettlementAssessmentOption(
                  id = "MENTORING_SUPPORT_ON_RELEASE",
                  displayText = "Mentoring support on release",
                  tag = "MENTORING_SUPPORT",
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
        title = "Children, families and communities report summary",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_NEEDS",
              title = "Children, families and communities resettlement status",
              subTitle = "Select one option.",
              type = TypeOfQuestion.RADIO,
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

  @ParameterizedTest
  @MethodSource("test complete assessment data")
  fun `test complete assessment`(assessmentType: ResettlementAssessmentType, assessment: ResettlementAssessmentCompleteRequest, expectedEntity: ResettlementAssessmentEntity?, expectedException: Throwable?, existingAssessment: ResettlementAssessmentEntity?) {
    mockkStatic(::getClaimFromJWTToken)
    every { getClaimFromJWTToken("string", "name") } returns "System user"
    every { getClaimFromJWTToken("string", "auth_source") } returns "nomis"
    every { getClaimFromJWTToken("string", "sub") } returns "USER_1"
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns testDate

    val nomsId = "abc"

    val prisonerEntity = PrisonerEntity(1, nomsId, testDate, "abc", "ABC")

    Mockito.lenient().`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)

    if (existingAssessment != null) {
      whenever(
        resettlementAssessmentRepository.findFirstByPrisonerIdAndPathwayAndAssessmentTypeAndAssessmentStatusInAndDeletedIsFalseOrderByCreationDateDesc(
          1,
          pathway,
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
    // Happy path - including a nested question answer with matching regex
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      getCompleteRequestWithNumberOfChildren("3"),
      ResettlementAssessmentEntity(
        id = null,
        prisonerId = 1,
        pathway = Pathway.CHILDREN_FAMILIES_AND_COMMUNITY,
        statusChangedTo = Status.SUPPORT_REQUIRED,
        assessmentType = ResettlementAssessmentType.BCST2,
        assessment = ResettlementAssessmentQuestionAndAnswerList(
          assessment = listOf(
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "PARTNER_OR_SPOUSE",
              answer = StringAnswer(answer = "YES"),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "PARENTAL_RESPONSIBILITY",
              answer = StringAnswer(answer = "YES"),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "PRIMARY_CARER_FOR_CHILDREN",
              answer = StringAnswer(answer = "YES"),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "NUMBER_OF_CHILDREN",
              answer = StringAnswer(answer = "3"),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "CHILDREN_SERVICE_INVOLVED",
              answer = StringAnswer(answer = "NO"),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "CARING_RESPONSIBILITIES_FOR_ADULTS",
              answer = StringAnswer(answer = "NO"),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "SOCIAL_SERVICE_INVOLVED",
              answer = StringAnswer(answer = "NO"),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "RECEIVED_SUPPORT_FROM_SOCIAL_SERVICES",
              answer = StringAnswer(answer = "NO"),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "DO_THEY_HAVE_SUPPORT_FROM_FAMILY_FRIENDS_COMMUNITY",
              answer = StringAnswer(answer = "NO"),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "INVOLVEMENT_IN_GANG_ACTIVITY",
              answer = StringAnswer(answer = "NO"),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "UNDER_THREAT_OUTSIDE_PRISON",
              answer = StringAnswer(answer = "NO"),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "VICTIM_OF_DOMESTIC_ABUSE",
              answer = StringAnswer(answer = "NO"),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "PERPETRATOR_OF_DOMESTIC_ABUSE",
              answer = StringAnswer(answer = "NO"),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "VICTIM_OF_SEXUAL_ABUSE",
              answer = StringAnswer(answer = "NO"),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "PERPETRATOR_OF_SEXUAL_ABUSE",
              answer = StringAnswer(answer = "NO"),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "WORKED_IN_SEX_INDUSTRY",
              answer = StringAnswer(answer = "NO"),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "SUPPORT_REQUIREMENTS",
              answer = ListAnswer(answer = listOf("SUPPORT_WHEN_MEETING_CHILDREN_SERVICES", "INTERNAL_SUPPORT_SERVICES")),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "SUPPORT_REQUIREMENTS_ADDITIONAL_DETAILS",
              answer = StringAnswer(answer = "Long text field answer"),
            ),
            ResettlementAssessmentSimpleQuestionAndAnswer(
              questionId = "SUPPORT_NEEDS",
              answer = StringAnswer(answer = "SUPPORT_REQUIRED"),
            ),
          ),
        ),
        creationDate = LocalDateTime.parse("2023-08-16T12:00:00.000"),
        createdBy = "System user",
        assessmentStatus = ResettlementAssessmentStatus.COMPLETE,
        caseNoteText = null,
        createdByUserId = "USER_1",
        version = 2,
        submissionDate = null,
        userDeclaration = true,
      ),
      null,
      null,
    ),
    // Throw exception if NUMBER_OF_CHILDREN answer does not match regex (i.e. not numerical)
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      getCompleteRequestWithNumberOfChildren("not a number"),
      null,
      ServerWebInputException("Invalid answer to question [NUMBER_OF_CHILDREN] as failed to match regex [^(?:[1-9])(\\d+)?$]"),
      null,
    ),
    // Throw exception if NUMBER_OF_CHILDREN answer is 0
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      getCompleteRequestWithNumberOfChildren("0"),
      null,
      ServerWebInputException("Invalid answer to question [NUMBER_OF_CHILDREN] as failed to match regex [^(?:[1-9])(\\d+)?$]"),
      null,
    ),
    // Throw exception if NUMBER_OF_CHILDREN answer is negative
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      getCompleteRequestWithNumberOfChildren("-2"),
      null,
      ServerWebInputException("Invalid answer to question [NUMBER_OF_CHILDREN] as failed to match regex [^(?:[1-9])(\\d+)?$]"),
      null,
    ),
    // Throw exception if NUMBER_OF_CHILDREN answer is not whole
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      getCompleteRequestWithNumberOfChildren("4.5"),
      null,
      ServerWebInputException("Invalid answer to question [NUMBER_OF_CHILDREN] as failed to match regex [^(?:[1-9])(\\d+)?$]"),
      null,
    ),
    // Throw exception if NUMBER_OF_CHILDREN answer is not empty
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      getCompleteRequestWithNumberOfChildren(""),
      null,
      ServerWebInputException("Invalid answer to question [NUMBER_OF_CHILDREN] as failed to match regex [^(?:[1-9])(\\d+)?$]"),
      null,
    ),
    // Throw exception if NUMBER_OF_CHILDREN answer is not blank
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      getCompleteRequestWithNumberOfChildren(" "),
      null,
      ServerWebInputException("Invalid answer to question [NUMBER_OF_CHILDREN] as failed to match regex [^(?:[1-9])(\\d+)?$]"),
      null,
    ),
  )

  private fun getCompleteRequestWithNumberOfChildren(numberOfChildren: String) = ResettlementAssessmentCompleteRequest(
    version = 2,
    questionsAndAnswers = listOf(
      ResettlementAssessmentRequestQuestionAndAnswer(
        question = "PARTNER_OR_SPOUSE",
        answer = StringAnswer("YES"),
      ),
      ResettlementAssessmentRequestQuestionAndAnswer(
        question = "PARENTAL_RESPONSIBILITY",
        answer = StringAnswer("YES"),
      ),
      ResettlementAssessmentRequestQuestionAndAnswer(
        question = "PRIMARY_CARER_FOR_CHILDREN",
        answer = StringAnswer("YES"),
      ),
      ResettlementAssessmentRequestQuestionAndAnswer(
        question = "NUMBER_OF_CHILDREN",
        answer = StringAnswer(numberOfChildren),
      ),
      ResettlementAssessmentRequestQuestionAndAnswer(
        question = "CHILDREN_SERVICE_INVOLVED",
        answer = StringAnswer("NO"),
      ),
      ResettlementAssessmentRequestQuestionAndAnswer(
        question = "CARING_RESPONSIBILITIES_FOR_ADULTS",
        answer = StringAnswer("NO"),
      ),
      ResettlementAssessmentRequestQuestionAndAnswer(
        question = "SOCIAL_SERVICE_INVOLVED",
        answer = StringAnswer("NO"),
      ),
      ResettlementAssessmentRequestQuestionAndAnswer(
        question = "RECEIVED_SUPPORT_FROM_SOCIAL_SERVICES",
        answer = StringAnswer("NO"),
      ),
      ResettlementAssessmentRequestQuestionAndAnswer(
        question = "DO_THEY_HAVE_SUPPORT_FROM_FAMILY_FRIENDS_COMMUNITY",
        answer = StringAnswer("NO"),
      ),
      ResettlementAssessmentRequestQuestionAndAnswer(
        question = "INVOLVEMENT_IN_GANG_ACTIVITY",
        answer = StringAnswer("NO"),
      ),
      ResettlementAssessmentRequestQuestionAndAnswer(
        question = "UNDER_THREAT_OUTSIDE_PRISON",
        answer = StringAnswer("NO"),
      ),
      ResettlementAssessmentRequestQuestionAndAnswer(
        question = "VICTIM_OF_DOMESTIC_ABUSE",
        answer = StringAnswer("NO"),
      ),
      ResettlementAssessmentRequestQuestionAndAnswer(
        question = "PERPETRATOR_OF_DOMESTIC_ABUSE",
        answer = StringAnswer("NO"),
      ),
      ResettlementAssessmentRequestQuestionAndAnswer(
        question = "VICTIM_OF_SEXUAL_ABUSE",
        answer = StringAnswer("NO"),
      ),
      ResettlementAssessmentRequestQuestionAndAnswer(
        question = "PERPETRATOR_OF_SEXUAL_ABUSE",
        answer = StringAnswer("NO"),
      ),
      ResettlementAssessmentRequestQuestionAndAnswer(
        question = "WORKED_IN_SEX_INDUSTRY",
        answer = StringAnswer("NO"),
      ),
      ResettlementAssessmentRequestQuestionAndAnswer(
        question = "SUPPORT_REQUIREMENTS",
        answer = ListAnswer(listOf("SUPPORT_WHEN_MEETING_CHILDREN_SERVICES", "INTERNAL_SUPPORT_SERVICES")),
      ),
      ResettlementAssessmentRequestQuestionAndAnswer(
        question = "SUPPORT_REQUIREMENTS_ADDITIONAL_DETAILS",
        answer = StringAnswer(answer = "Long text field answer"),
      ),
      ResettlementAssessmentRequestQuestionAndAnswer(
        question = "SUPPORT_NEEDS",
        answer = StringAnswer("SUPPORT_REQUIRED"),
      ),
    ),
  )
}
