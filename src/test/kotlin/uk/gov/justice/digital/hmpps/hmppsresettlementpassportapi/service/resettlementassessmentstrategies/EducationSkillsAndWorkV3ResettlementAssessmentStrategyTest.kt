package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentOption
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Validation
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ValidationType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.yesNoOptions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import java.util.stream.Stream

class EducationSkillsAndWorkV3ResettlementAssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.EDUCATION_SKILLS_AND_WORK, 3) {

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
      pathway = Pathway.EDUCATION_SKILLS_AND_WORK,
      assessmentType = ResettlementAssessmentType.BCST2,
      currentPage = currentPage,
      version = 3,
    )
    Assertions.assertEquals(expectedPage, nextPage)
  }

  private fun `test next page function flow - no existing assessment data`() = Stream.of(
    // Start of flow - send null current page to get first page
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      null,
      "EDUCATION_SKILLS_AND_WORK_REPORT",
    ),
    // Any answer to EDUCATION_SKILLS_AND_WORK_REPORT, go to SUPPORT_REQUIREMENTS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      "EDUCATION_SKILLS_AND_WORK_REPORT",
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
      pathway = Pathway.EDUCATION_SKILLS_AND_WORK,
      assessmentType = ResettlementAssessmentType.BCST2,
      pageId = pageIdInput,
      version = 3,
    )
    Assertions.assertEquals(expectedPage, page)
  }

  private fun `test get page from Id - no existing assessment data`() = Stream.of(
    Arguments.of(
      "EDUCATION_SKILLS_AND_WORK_REPORT",
      ResettlementAssessmentResponsePage(
        id = "EDUCATION_SKILLS_AND_WORK_REPORT",
        title = "Education, skills and work report",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "DID_THEY_HAVE_JOB_BEFORE_CUSTODY",
              title = "Did the person in prison have a job before custody?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = listOf(
                ResettlementAssessmentOption(
                  id = "YES",
                  displayText = "Yes",
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "DID_THEY_HAVE_JOB_BEFORE_CUSTODY_JOB_TITLE",
                        title = "Job title",
                        type = TypeOfQuestion.SHORT_TEXT,
                      ),
                      originalPageId = "EDUCATION_SKILLS_AND_WORK_REPORT",
                    ),
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "DID_THEY_HAVE_JOB_BEFORE_CUSTODY_EMPLOYER_NAME",
                        title = "Employer name",
                        type = TypeOfQuestion.SHORT_TEXT,
                      ),
                      originalPageId = "EDUCATION_SKILLS_AND_WORK_REPORT",
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
            originalPageId = "EDUCATION_SKILLS_AND_WORK_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "DID_THEY_HAVE_JOB_BEFORE_CUSTODY_ADDITIONAL_DETAILS",
              title = "Additional details",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "EDUCATION_SKILLS_AND_WORK_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "DO_THEY_HAVE_JOB_ARRANGED",
              title = "Does the person in prison have a job arranged for when they are released?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = listOf(
                ResettlementAssessmentOption(
                  id = "YES_RETURNING_TO_SAME_JOB",
                  displayText = "Yes, returning to same job",
                ),
                ResettlementAssessmentOption(
                  id = "YES_STARTING_NEW_JOB",
                  displayText = "Yes, starting new job",
                  nestedQuestions = listOf(
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "STARTING_NEW_JOB_JOB_TITLE",
                        title = "Job title",
                        type = TypeOfQuestion.SHORT_TEXT,
                      ),
                      originalPageId = "EDUCATION_SKILLS_AND_WORK_REPORT",
                    ),
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "STARTING_NEW_JOB_EMPLOYER_NAME",
                        title = "Employer name",
                        type = TypeOfQuestion.SHORT_TEXT,
                      ),
                      originalPageId = "EDUCATION_SKILLS_AND_WORK_REPORT",
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
            originalPageId = "EDUCATION_SKILLS_AND_WORK_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "DO_THEY_HAVE_JOB_ARRANGED_ADDITIONAL_DETAILS",
              title = "Additional details",
              subTitle = "Specify if they were employed full or part time or whether they were self-employed. Specify if they had a permanent, temporary, fixed-term or zero hours contract.",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "EDUCATION_SKILLS_AND_WORK_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "WERE_THEY_IN_EDUCATION_BEFORE_CUSTODY",
              title = "Was the person in prison in education or training before custody?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "EDUCATION_SKILLS_AND_WORK_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "WERE_THEY_IN_EDUCATION_BEFORE_CUSTODY_ADDITIONAL_DETAILS",
              title = "Additional details",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "EDUCATION_SKILLS_AND_WORK_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "EDUCATION_WHEN_RELEASED",
              title = "Does the person in prison have education or training in place for when they are released?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              options = listOf(
                ResettlementAssessmentOption(
                  id = "YES_SAME_EDUCATION",
                  displayText = "Yes, returning to same education or training",
                ),
                ResettlementAssessmentOption(
                  id = "YES_STARTING_NEW_EDUCATION",
                  displayText = "Yes, starting new education or training",
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
            originalPageId = "EDUCATION_SKILLS_AND_WORK_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "EDUCATION_WHEN_RELEASED_ADDITIONAL_DETAILS",
              title = "Additional details",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "EDUCATION_SKILLS_AND_WORK_REPORT",
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
                  id = "HELP_TO_FIND_JOB",
                  displayText = "Help to find a job for release",
                  tag = "FIND_A_JOB",
                ),
                ResettlementAssessmentOption(
                  id = "SUPPORT_TO_CONTACT_EMPLOYER_FROM_BEFORE_CUSTODY",
                  displayText = "Support contacting employer from before custody",
                ),
                ResettlementAssessmentOption(
                  id = "SUPPORT_TO_CONTACT_EMPLOYER_FOR_AFTER_RELEASE",
                  displayText = "Support contacting employer for after release",
                  tag = "CONTACT_EMPLOYER",
                ),
                ResettlementAssessmentOption(
                  id = "HELP_FIND_EDUCATION_FOR_RELEASE",
                  displayText = "Help to find education or training for release",
                  tag = "FIND_EDUCATION_TRAINING",
                ),
                ResettlementAssessmentOption(
                  id = "SUPPORT_CONTACTING_TRAINING_PROVIDER",
                  displayText = "Support contacting an education or training provider",
                  tag = "CONTACT_EDUCATION_TRAINING",
                ),
                ResettlementAssessmentOption(
                  id = "INFORMATION_ABOUT_GRANTS_AND_TRAINING",
                  displayText = "Information about bursaries and grants for courses or training",
                  tag = "BURSARIES_AND_GRANTS_TRAINING",
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
        title = "Education, skills and work report summary",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_NEEDS",
              title = "Education, skills and work resettlement status",
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
}
