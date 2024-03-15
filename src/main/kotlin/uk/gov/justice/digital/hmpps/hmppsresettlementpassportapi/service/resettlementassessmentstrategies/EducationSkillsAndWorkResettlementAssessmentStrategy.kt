package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerWebInputException
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IAssessmentPage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.IResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Option
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentNode
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ValidationType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.yesNoOptions
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PrisonerRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentStatusRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.StatusRepository

@Service
class EducationSkillsAndWorkResettlementAssessmentStrategy(
  resettlementAssessmentRepository: ResettlementAssessmentRepository,
  prisonerRepository: PrisonerRepository,
  statusRepository: StatusRepository,
  pathwayRepository: PathwayRepository,
  resettlementAssessmentStatusRepository: ResettlementAssessmentStatusRepository,
) : AbstractResettlementAssessmentStrategy<EducationSkillsAndWorkAssessmentPage, EducationSkillsAndWorkResettlementAssessmentQuestion>(resettlementAssessmentRepository, prisonerRepository, statusRepository, pathwayRepository, resettlementAssessmentStatusRepository, EducationSkillsAndWorkAssessmentPage::class, EducationSkillsAndWorkResettlementAssessmentQuestion::class) {
  override fun appliesTo(pathway: Pathway) = pathway == Pathway.EDUCATION_SKILLS_AND_WORK

  override fun getPageList(): List<ResettlementAssessmentNode> = listOf(
    ResettlementAssessmentNode(
      EducationSkillsAndWorkAssessmentPage.JOB_BEFORE_CUSTODY,
      nextPage =
      fun(currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, _: Boolean): IAssessmentPage {
        return if (currentQuestionsAndAnswers.any { it.question == EducationSkillsAndWorkResettlementAssessmentQuestion.JOB_BEFORE_CUSTODY && it.answer?.answer is String && (it.answer!!.answer as String == "YES") }) {
          EducationSkillsAndWorkAssessmentPage.TYPE_OF_EMPLOYMENT_CONTRACT
        } else if (currentQuestionsAndAnswers.any { it.question == EducationSkillsAndWorkResettlementAssessmentQuestion.JOB_BEFORE_CUSTODY && (it.answer?.answer as String in listOf("NO", "NO_ANSWER")) }) {
          EducationSkillsAndWorkAssessmentPage.HAVE_A_JOB_AFTER_RELEASE
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${EducationSkillsAndWorkResettlementAssessmentQuestion.JOB_BEFORE_CUSTODY}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      EducationSkillsAndWorkAssessmentPage.TYPE_OF_EMPLOYMENT_CONTRACT,
      nextPage =
      fun(_: List<ResettlementAssessmentQuestionAndAnswer>, _: Boolean): IAssessmentPage {
        return EducationSkillsAndWorkAssessmentPage.RETURN_TO_JOB_AFTER_RELEASE
      },
    ),
    ResettlementAssessmentNode(
      EducationSkillsAndWorkAssessmentPage.RETURN_TO_JOB_AFTER_RELEASE,
      nextPage =
      fun(currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, _: Boolean): IAssessmentPage {
        return if (currentQuestionsAndAnswers.any { it.question == EducationSkillsAndWorkResettlementAssessmentQuestion.RETURN_TO_JOB_AFTER_RELEASE && it.answer?.answer is String && (it.answer!!.answer as String == "YES") }) {
          EducationSkillsAndWorkAssessmentPage.HELP_CONTACTING_EMPLOYER
        } else if (currentQuestionsAndAnswers.any { it.question == EducationSkillsAndWorkResettlementAssessmentQuestion.RETURN_TO_JOB_AFTER_RELEASE && (it.answer?.answer as String in listOf("NO", "NO_ANSWER")) }) {
          EducationSkillsAndWorkAssessmentPage.HAVE_A_JOB_AFTER_RELEASE
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${EducationSkillsAndWorkResettlementAssessmentQuestion.RETURN_TO_JOB_AFTER_RELEASE}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      EducationSkillsAndWorkAssessmentPage.HAVE_A_JOB_AFTER_RELEASE,
      nextPage =
      fun(currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, _: Boolean): IAssessmentPage {
        return if (currentQuestionsAndAnswers.any { it.question == EducationSkillsAndWorkResettlementAssessmentQuestion.HAVE_A_JOB_AFTER_RELEASE && it.answer?.answer is String && (it.answer!!.answer as String == "YES") }) {
          EducationSkillsAndWorkAssessmentPage.HELP_CONTACTING_EMPLOYER
        } else if (currentQuestionsAndAnswers.any { it.question == EducationSkillsAndWorkResettlementAssessmentQuestion.HAVE_A_JOB_AFTER_RELEASE && (it.answer?.answer as String in listOf("NO", "NO_ANSWER")) }) {
          EducationSkillsAndWorkAssessmentPage.SUPPORT_TO_FIND_JOB
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${EducationSkillsAndWorkResettlementAssessmentQuestion.HAVE_A_JOB_AFTER_RELEASE}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      EducationSkillsAndWorkAssessmentPage.HELP_CONTACTING_EMPLOYER,
      nextPage =
      fun(currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, _: Boolean): IAssessmentPage {
        return if (currentQuestionsAndAnswers.any { it.question == EducationSkillsAndWorkResettlementAssessmentQuestion.HELP_CONTACTING_EMPLOYER && it.answer?.answer is String && (it.answer!!.answer as String == "YES") }) {
          EducationSkillsAndWorkAssessmentPage.EMPLOYMENT_DETAILS_BEFORE_CUSTODY
        } else if (currentQuestionsAndAnswers.any { it.question == EducationSkillsAndWorkResettlementAssessmentQuestion.HELP_CONTACTING_EMPLOYER && (it.answer?.answer as String in listOf("NO", "NO_ANSWER")) }) {
          EducationSkillsAndWorkAssessmentPage.IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${EducationSkillsAndWorkResettlementAssessmentQuestion.HELP_CONTACTING_EMPLOYER}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      EducationSkillsAndWorkAssessmentPage.EMPLOYMENT_DETAILS_BEFORE_CUSTODY,
      nextPage =
      fun(_: List<ResettlementAssessmentQuestionAndAnswer>, _: Boolean): IAssessmentPage {
        return EducationSkillsAndWorkAssessmentPage.IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY
      },
    ),
    ResettlementAssessmentNode(
      EducationSkillsAndWorkAssessmentPage.SUPPORT_TO_FIND_JOB,
      nextPage =
      fun(_: List<ResettlementAssessmentQuestionAndAnswer>, _: Boolean): IAssessmentPage {
        return EducationSkillsAndWorkAssessmentPage.IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY
      },
    ),
    ResettlementAssessmentNode(
      EducationSkillsAndWorkAssessmentPage.IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY,
      nextPage =
      fun(currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, _: Boolean): IAssessmentPage {
        return if (currentQuestionsAndAnswers.any { it.question == EducationSkillsAndWorkResettlementAssessmentQuestion.IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY && it.answer?.answer is String && (it.answer!!.answer as String == "YES") }) {
          EducationSkillsAndWorkAssessmentPage.RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE
        } else if (currentQuestionsAndAnswers.any { it.question == EducationSkillsAndWorkResettlementAssessmentQuestion.IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY && (it.answer?.answer as String in listOf("NO", "NO_ANSWER")) }) {
          EducationSkillsAndWorkAssessmentPage.WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${EducationSkillsAndWorkResettlementAssessmentQuestion.IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      EducationSkillsAndWorkAssessmentPage.RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE,
      nextPage =
      fun(currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, _: Boolean): IAssessmentPage {
        return if (currentQuestionsAndAnswers.any { it.question == EducationSkillsAndWorkResettlementAssessmentQuestion.RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE && it.answer?.answer is String && (it.answer!!.answer as String == "NO") }) {
          EducationSkillsAndWorkAssessmentPage.WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE
        } else if (currentQuestionsAndAnswers.any { it.question == EducationSkillsAndWorkResettlementAssessmentQuestion.RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE && (it.answer?.answer as String in listOf("YES", "NO_ANSWER")) }) {
          EducationSkillsAndWorkAssessmentPage.HELP_CONTACTING_EDUCATION_PROVIDER
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${EducationSkillsAndWorkResettlementAssessmentQuestion.RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      EducationSkillsAndWorkAssessmentPage.WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE,
      nextPage =
      fun(currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, edit: Boolean): IAssessmentPage {
        return if (currentQuestionsAndAnswers.any { it.question == EducationSkillsAndWorkResettlementAssessmentQuestion.WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE && it.answer?.answer is String && (it.answer!!.answer as String == "YES") }) {
          EducationSkillsAndWorkAssessmentPage.BURSARIES_AND_GRANTS
        } else if (currentQuestionsAndAnswers.any { it.question == EducationSkillsAndWorkResettlementAssessmentQuestion.WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE && (it.answer?.answer as String in listOf("NO", "NO_ANSWER")) }) {
          finalQuestionNextPage(currentQuestionsAndAnswers, edit)
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${EducationSkillsAndWorkResettlementAssessmentQuestion.WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      EducationSkillsAndWorkAssessmentPage.HELP_CONTACTING_EDUCATION_PROVIDER,
      nextPage =
      fun(currentQuestionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, _: Boolean): IAssessmentPage {
        return if (currentQuestionsAndAnswers.any { it.question == EducationSkillsAndWorkResettlementAssessmentQuestion.HELP_CONTACTING_EDUCATION_PROVIDER && it.answer?.answer is String && (it.answer!!.answer as String == "YES") }) {
          EducationSkillsAndWorkAssessmentPage.TRAINING_PROVIDER_DETAILS
        } else if (currentQuestionsAndAnswers.any { it.question == EducationSkillsAndWorkResettlementAssessmentQuestion.HELP_CONTACTING_EDUCATION_PROVIDER && (it.answer?.answer as String in listOf("NO", "NO_ANSWER")) }) {
          EducationSkillsAndWorkAssessmentPage.BURSARIES_AND_GRANTS
        } else {
          // Bad request if the question isn't answered
          throw ServerWebInputException("No valid answer found to mandatory question ${EducationSkillsAndWorkResettlementAssessmentQuestion.HELP_CONTACTING_EDUCATION_PROVIDER}.")
        }
      },
    ),
    ResettlementAssessmentNode(
      EducationSkillsAndWorkAssessmentPage.TRAINING_PROVIDER_DETAILS,
      nextPage =
      fun(_: List<ResettlementAssessmentQuestionAndAnswer>, _: Boolean): IAssessmentPage {
        return EducationSkillsAndWorkAssessmentPage.BURSARIES_AND_GRANTS
      },
    ),
    ResettlementAssessmentNode(
      EducationSkillsAndWorkAssessmentPage.BURSARIES_AND_GRANTS,
      nextPage = ::finalQuestionNextPage,
    ),
    assessmentSummaryNode,
  )
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class EducationSkillsAndWorkAssessmentPage(override val id: String, override val questionsAndAnswers: List<ResettlementAssessmentQuestionAndAnswer>, override val title: String? = null) :
  IAssessmentPage {
  JOB_BEFORE_CUSTODY(id = "JOB_BEFORE_CUSTODY", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(EducationSkillsAndWorkResettlementAssessmentQuestion.JOB_BEFORE_CUSTODY))),
  TYPE_OF_EMPLOYMENT_CONTRACT(id = "TYPE_OF_EMPLOYMENT_CONTRACT", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(EducationSkillsAndWorkResettlementAssessmentQuestion.TYPE_OF_EMPLOYMENT_CONTRACT))),
  RETURN_TO_JOB_AFTER_RELEASE(id = "RETURN_TO_JOB_AFTER_RELEASE", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(EducationSkillsAndWorkResettlementAssessmentQuestion.RETURN_TO_JOB_AFTER_RELEASE))),
  HAVE_A_JOB_AFTER_RELEASE(id = "HAVE_A_JOB_AFTER_RELEASE", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(EducationSkillsAndWorkResettlementAssessmentQuestion.HAVE_A_JOB_AFTER_RELEASE))),
  HELP_CONTACTING_EMPLOYER(id = "HELP_CONTACTING_EMPLOYER", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(EducationSkillsAndWorkResettlementAssessmentQuestion.HELP_CONTACTING_EMPLOYER))),
  EMPLOYMENT_DETAILS_BEFORE_CUSTODY(id = "EMPLOYMENT_DETAILS_BEFORE_CUSTODY", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(EducationSkillsAndWorkResettlementAssessmentQuestion.EMPLOYMENT_TITLE_BEFORE_CUSTODY), ResettlementAssessmentQuestionAndAnswer(EducationSkillsAndWorkResettlementAssessmentQuestion.NAME_OF_EMPLOYER), ResettlementAssessmentQuestionAndAnswer(EducationSkillsAndWorkResettlementAssessmentQuestion.ADDRESS_OF_EMPLOYER)), title = "Employment before custody"),
  SUPPORT_TO_FIND_JOB(id = "SUPPORT_TO_FIND_JOB", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(EducationSkillsAndWorkResettlementAssessmentQuestion.SUPPORT_TO_FIND_JOB))),
  IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY(id = "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(EducationSkillsAndWorkResettlementAssessmentQuestion.IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY))),
  RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE(id = "RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(EducationSkillsAndWorkResettlementAssessmentQuestion.RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE))),
  WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE(id = "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(EducationSkillsAndWorkResettlementAssessmentQuestion.WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE))),
  HELP_CONTACTING_EDUCATION_PROVIDER(id = "HELP_CONTACTING_EDUCATION_PROVIDER", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(EducationSkillsAndWorkResettlementAssessmentQuestion.HELP_CONTACTING_EDUCATION_PROVIDER))),
  TRAINING_PROVIDER_DETAILS(id = "TRAINING_PROVIDER_DETAILS", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(EducationSkillsAndWorkResettlementAssessmentQuestion.NAME_OF_TRAINING_PROVIDER), ResettlementAssessmentQuestionAndAnswer(EducationSkillsAndWorkResettlementAssessmentQuestion.ADDRESS_OF_TRAINING_PROVIDER)), title = "Education or training before custody"),
  BURSARIES_AND_GRANTS(id = "BURSARIES_AND_GRANTS", questionsAndAnswers = listOf(ResettlementAssessmentQuestionAndAnswer(EducationSkillsAndWorkResettlementAssessmentQuestion.BURSARIES_AND_GRANTS))),
}

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class EducationSkillsAndWorkResettlementAssessmentQuestion(
  override val id: String,
  override val title: String,
  override val subTitle: String? = null,
  override val type: TypeOfQuestion,
  override val options: List<Option>? = null,
  override val validationType: ValidationType = ValidationType.MANDATORY,
) : IResettlementAssessmentQuestion {
  JOB_BEFORE_CUSTODY(
    id = "JOB_BEFORE_CUSTODY",
    title = "Did the person in prison have a job before custody?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  TYPE_OF_EMPLOYMENT_CONTRACT(
    id = "TYPE_OF_EMPLOYMENT_CONTRACT",
    title = "Type of employment contract",
    type = TypeOfQuestion.CHECKBOX,
    options = listOf(
      Option(id = "FULL_TIME_CONTRACT", displayText = "Full-time contract"),
      Option(id = "PART_TIME_CONTRACT", displayText = "Part-time contract"),
      Option(id = "PERMANENT_CONTRACT", displayText = "Permanent contract"),
      Option(id = "TEMPORARY_CONTRACT", displayText = "Temporary contract"),
      Option(id = "FIXED_TERM_CONTRACT", displayText = "Fixed-term contract"),
      Option(id = "ZERO_HOURS_CONTRACT", displayText = "Zero hours contract"),
      Option(id = "NO_ANSWER", displayText = "No answer provided", exclusive = true),
    ),
  ),
  RETURN_TO_JOB_AFTER_RELEASE(
    id = "RETURN_TO_JOB_AFTER_RELEASE",
    title = "Can the person in prison return to this job after release?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  HAVE_A_JOB_AFTER_RELEASE(
    id = "HAVE_A_JOB_AFTER_RELEASE",
    title = "Does the person in prison have a job when they are released?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  HELP_CONTACTING_EMPLOYER(
    id = "HELP_CONTACTING_EMPLOYER",
    title = "Does the person in prison need help contacting the employer?",
    type = TypeOfQuestion.RADIO,
    options = listOf(
      Option(id = "YES", displayText = "Yes"),
      Option(id = "NO", displayText = "No"),
      Option(id = "NO_ANSWER", displayText = "No answer provided"),
    ),
  ),
  EMPLOYMENT_TITLE_BEFORE_CUSTODY(
    id = "EMPLOYMENT_TITLE_BEFORE_CUSTODY",
    title = "Job title",
    type = TypeOfQuestion.SHORT_TEXT,
  ),
  NAME_OF_EMPLOYER(
    id = "NAME_OF_EMPLOYER",
    title = "Employer",
    type = TypeOfQuestion.SHORT_TEXT,
  ),
  ADDRESS_OF_EMPLOYER(
    id = "ADDRESS_OF_EMPLOYER",
    title = "Employer address",
    type = TypeOfQuestion.ADDRESS,
  ),
  SUPPORT_TO_FIND_JOB(
    id = "SUPPORT_TO_FIND_JOB",
    title = "Does the person in prison want support to find a job when they are released?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY(
    id = "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
    title = "Was the person in prison in education or training before custody?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE(
    id = "RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE",
    title = "Can the person in prison return to this education or training after release?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE(
    id = "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
    title = "Does the person in prison want to start education or training after release?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  HELP_CONTACTING_EDUCATION_PROVIDER(
    id = "HELP_CONTACTING_EDUCATION_PROVIDER",
    title = "Does the person in prison want help contacting an education or training provider?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
  NAME_OF_TRAINING_PROVIDER(
    id = "NAME_OF_TRAINING_PROVIDER",
    title = "Education or training provider",
    type = TypeOfQuestion.SHORT_TEXT,
  ),
  ADDRESS_OF_TRAINING_PROVIDER(
    id = "ADDRESS_OF_TRAINING_PROVIDER",
    title = "Education or training provider address",
    type = TypeOfQuestion.ADDRESS,
  ),
  BURSARIES_AND_GRANTS(
    id = "BURSARIES_AND_GRANTS",
    title = "Does the person in prison want to find out about bursaries and grants for courses or training?",
    type = TypeOfQuestion.RADIO,
    options = yesNoOptions,
  ),
}
