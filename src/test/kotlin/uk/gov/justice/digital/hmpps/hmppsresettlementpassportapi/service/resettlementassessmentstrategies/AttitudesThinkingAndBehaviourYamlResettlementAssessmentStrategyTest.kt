package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponseQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentResponseQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.yesNoOptions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentQuestionAndAnswerList
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import java.time.LocalDate
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension::class)
class AttitudesThinkingAndBehaviourYamlResettlementAssessmentStrategyTest : YamlResettlementStrategyTest() {

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
    val nextPage = resettlementAssessmentService.getNextPageId(
      assessment = assessment,
      nomsId = nomsId,
      pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR,
      assessmentType = ResettlementAssessmentType.BCST2,
      currentPage = currentPage,
    )
    Assertions.assertEquals(expectedPage, nextPage)
  }

  private fun `test next page function flow - no existing assessment data`() = Stream.of(
    // Start of flow - send null current page to get first page
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      null,
      "HELP_TO_MANAGE_ANGER",
    ),
    // Any answer to HELP_TO_MANAGE_ANGER, go to ISSUES_WITH_GAMBLING
    Arguments.of(
      listOf(
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_TO_MANAGE_ANGER", answer = StringAnswer("YES")),
      ),
      "HELP_TO_MANAGE_ANGER",
      "ISSUES_WITH_GAMBLING",
    ),
    // Any answer to ISSUES_WITH_GAMBLING, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf(
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_TO_MANAGE_ANGER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("ISSUES_WITH_GAMBLING", answer = StringAnswer("NO_ANSWER")),
      ),
      "ISSUES_WITH_GAMBLING",
      "ASSESSMENT_SUMMARY",
    ),
    // Any answer to ASSESSMENT_SUMMARY, go to CHECK_ANSWERS
    Arguments.of(
      listOf(
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_TO_MANAGE_ANGER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("ISSUES_WITH_GAMBLING", answer = StringAnswer("NO_ANSWER")),
      ),
      "ASSESSMENT_SUMMARY",
      "CHECK_ANSWERS",
    ),
  )

  @ParameterizedTest(name = "{0} page")
  @MethodSource("test get page from Id - no existing assessment data")
  fun `test get page from Id - no existing assessment`(pageIdInput: String, expectedPage: ResettlementAssessmentResponsePage) {
    val nomsId = "123"
    setUpMocks("123", false)

    val page = resettlementAssessmentService.getPageFromId(
      nomsId = nomsId,
      pathway = Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = pageIdInput,
    )
    Assertions.assertEquals(expectedPage, page)
  }

  private fun `test get page from Id - no existing assessment data`() = Stream.of(
    Arguments.of(
      "HELP_TO_MANAGE_ANGER",
      ResettlementAssessmentResponsePage(
        id = "HELP_TO_MANAGE_ANGER",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "HELP_TO_MANAGE_ANGER",
              title = "Does the person in prison want support managing their emotions?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "HELP_TO_MANAGE_ANGER",
          ),
        ),
      ),
    ),
    Arguments.of(
      "ISSUES_WITH_GAMBLING",
      ResettlementAssessmentResponsePage(
        id = "ISSUES_WITH_GAMBLING",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "ISSUES_WITH_GAMBLING",
              title = "Does the person in prison want support with gambling issues?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions.toMutableList(),
            ),
            originalPageId = "ISSUES_WITH_GAMBLING",
          ),
        ),
      ),
    ),
    Arguments.of(
      "ASSESSMENT_SUMMARY",
      ResettlementAssessmentResponsePage(
        id = "ASSESSMENT_SUMMARY",
        questionsAndAnswers = mutableListOf(
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "SUPPORT_NEEDS",
              title = "",
              type = TypeOfQuestion.RADIO,
              options = mutableListOf(
                Option(
                  id = "SUPPORT_REQUIRED",
                  displayText = "Support required",
                  description = "a need for support has been identified and is accepted",
                ),
                Option(id = "SUPPORT_NOT_REQUIRED", displayText = "Support not required", description = "no need was identified"),
                Option(
                  id = "SUPPORT_DECLINED",
                  displayText = "Support declined",
                  description = "a need has been identified but support is declined",
                ),
              ),
            ),
            originalPageId = "ASSESSMENT_SUMMARY",
          ),
          ResettlementAssessmentResponseQuestionAndAnswer(
            question = ResettlementAssessmentResponseQuestion(
              id = "CASE_NOTE_SUMMARY",
              title = "Add a case note summary",
              subTitle = "This will be displayed as a case note in both DPS and nDelius",
              type = TypeOfQuestion.LONG_TEXT,
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
        questionsAndAnswers = mutableListOf(),
      ),
    ),
  )

  private fun setUpMocks(nomsId: String, returnResettlementAssessmentEntity: Boolean, assessment: ResettlementAssessmentQuestionAndAnswerList = ResettlementAssessmentQuestionAndAnswerList(mutableListOf())) {
    val prisonerEntity = PrisonerEntity(1, nomsId, testDate, "abc", "ABC", LocalDate.parse("2025-01-23"))
    val resettlementAssessmentEntity = if (returnResettlementAssessmentEntity) ResettlementAssessmentEntity(1, prisonerEntity, Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR, Status.NOT_STARTED, ResettlementAssessmentType.BCST2, assessment, testDate, "", ResettlementAssessmentStatus.COMPLETE, "some text", "USER_1", submissionDate = null) else null
    Mockito.`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)
    Mockito.`when`(
      resettlementAssessmentRepository.findFirstByPrisonerAndPathwayAndAssessmentTypeAndAssessmentStatusInOrderByCreationDateDesc(
        prisonerEntity,
        Pathway.ATTITUDES_THINKING_AND_BEHAVIOUR,
        ResettlementAssessmentType.BCST2,
        listOf(ResettlementAssessmentStatus.COMPLETE, ResettlementAssessmentStatus.SUBMITTED),
      ),
    ).thenReturn(resettlementAssessmentEntity)
  }
}
