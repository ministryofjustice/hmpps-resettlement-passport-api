package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.junit.jupiter.params.provider.Arguments
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentOption
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Validation
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ValidationType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.yesNoOptions
import java.util.stream.Stream

class EducationSkillsAndWorkV4ResettlementAssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.EDUCATION_SKILLS_AND_WORK, 4) {

  override fun `test next page function flow - no existing assessment data`(): Stream<Arguments> = Stream.of(
    // Start of flow - send null current page to get first page
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      null,
      "EDUCATION_SKILLS_AND_WORK_REPORT",
    ),
    // Any answer to EDUCATION_SKILLS_AND_WORK_REPORT, go to CHECK_ANSWERS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      "EDUCATION_SKILLS_AND_WORK_REPORT",
      "CHECK_ANSWERS",
    ),
  )

  override fun `test get page from Id - no existing assessment data`(): Stream<Arguments> = Stream.of(
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
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison had a job before custody"),
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
                        validation = Validation(ValidationType.MANDATORY, message = "Enter job title"),
                      ),
                      originalPageId = "EDUCATION_SKILLS_AND_WORK_REPORT",
                    ),
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "DID_THEY_HAVE_JOB_BEFORE_CUSTODY_EMPLOYER_NAME",
                        title = "Employer name",
                        type = TypeOfQuestion.SHORT_TEXT,
                        validation = Validation(ValidationType.MANDATORY, message = "Enter employer name"),
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
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison has a job arranged for when they are released"),
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
                        validation = Validation(ValidationType.MANDATORY, message = "Enter job title"),
                      ),
                      originalPageId = "EDUCATION_SKILLS_AND_WORK_REPORT",
                    ),
                    ResettlementAssessmentQuestionAndAnswer(
                      question = ResettlementAssessmentQuestion(
                        id = "STARTING_NEW_JOB_EMPLOYER_NAME",
                        title = "Employer name",
                        type = TypeOfQuestion.SHORT_TEXT,
                        validation = Validation(ValidationType.MANDATORY, message = "Enter employer name"),
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
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison was in education or training before custody"),
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
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison has education or training in place for when they are released"),
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
      "CHECK_ANSWERS",
      ResettlementAssessmentResponsePage(
        id = "CHECK_ANSWERS",
        questionsAndAnswers = listOf(),
      ),
    ),
  )
}
