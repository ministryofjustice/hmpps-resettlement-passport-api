package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.resettlementassessmentstrategies

import org.junit.jupiter.params.provider.Arguments
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentOption
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentResponsePage
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.TypeOfQuestion
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers.yesNoOptions
import java.util.stream.Stream

class EducationSkillsAndWorkV1ResettlementAssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.EDUCATION_SKILLS_AND_WORK, 1) {

  override fun `test next page function flow - no existing assessment data`(): Stream<Arguments> = Stream.of(
    // Start of flow - send null current page to get first page
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      null,
      "JOB_BEFORE_CUSTODY",
    ),
    // If the answer to JOB_BEFORE_CUSTODY is YES, go to TYPE_OF_EMPLOYMENT_CONTRACT
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("YES")),
      ),
      "JOB_BEFORE_CUSTODY",
      "TYPE_OF_EMPLOYMENT_CONTRACT",
    ),
    // If the answer to JOB_BEFORE_CUSTODY is NO, go to HAVE_A_JOB_AFTER_RELEASE
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
      ),
      "JOB_BEFORE_CUSTODY",
      "HAVE_A_JOB_AFTER_RELEASE",
    ),
    // If the answer to JOB_BEFORE_CUSTODY is NO_ANSWER, go to HAVE_A_JOB_AFTER_RELEASE
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO_ANSWER")),
      ),
      "JOB_BEFORE_CUSTODY",
      "HAVE_A_JOB_AFTER_RELEASE",
    ),
    // Any answer to TYPE_OF_EMPLOYMENT_CONTRACT, go to RETURN_TO_JOB_AFTER_RELEASE
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO_ANSWER")),
        ResettlementAssessmentRequestQuestionAndAnswer("TYPE_OF_EMPLOYMENT_CONTRACT", answer = ListAnswer(listOf("FULL_TIME_CONTRACT", "PERMANENT_CONTRACT"))),
      ),
      "TYPE_OF_EMPLOYMENT_CONTRACT",
      "RETURN_TO_JOB_AFTER_RELEASE",
    ),
    // If the answer to RETURN_TO_JOB_AFTER_RELEASE is YES, go to HELP_CONTACTING_EMPLOYER
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("TYPE_OF_EMPLOYMENT_CONTRACT", answer = ListAnswer(listOf("FULL_TIME_CONTRACT", "PERMANENT_CONTRACT"))),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
      ),
      "RETURN_TO_JOB_AFTER_RELEASE",
      "HELP_CONTACTING_EMPLOYER",
    ),
    // If the answer to RETURN_TO_JOB_AFTER_RELEASE is NO, go to HAVE_A_JOB_AFTER_RELEASE
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("TYPE_OF_EMPLOYMENT_CONTRACT", answer = ListAnswer(listOf("FULL_TIME_CONTRACT", "PERMANENT_CONTRACT"))),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_JOB_AFTER_RELEASE", answer = StringAnswer("NO")),
      ),
      "RETURN_TO_JOB_AFTER_RELEASE",
      "HAVE_A_JOB_AFTER_RELEASE",
    ),
    // If the answer to RETURN_TO_JOB_AFTER_RELEASE is NO_ANSWER, go to HAVE_A_JOB_AFTER_RELEASE
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("TYPE_OF_EMPLOYMENT_CONTRACT", answer = ListAnswer(listOf("FULL_TIME_CONTRACT", "PERMANENT_CONTRACT"))),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_JOB_AFTER_RELEASE", answer = StringAnswer("NO_ANSWER")),
      ),
      "RETURN_TO_JOB_AFTER_RELEASE",
      "HAVE_A_JOB_AFTER_RELEASE",
    ),
    // If the answer to HAVE_A_JOB_AFTER_RELEASE is YES, go to HELP_CONTACTING_EMPLOYER
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
      ),
      "HAVE_A_JOB_AFTER_RELEASE",
      "HELP_CONTACTING_EMPLOYER",
    ),
    // If the answer to HAVE_A_JOB_AFTER_RELEASE is NO, go to SUPPORT_TO_FIND_JOB
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("NO")),
      ),
      "HAVE_A_JOB_AFTER_RELEASE",
      "SUPPORT_TO_FIND_JOB",
    ),
    // If the answer to HAVE_A_JOB_AFTER_RELEASE is NO_ANSWER, go to SUPPORT_TO_FIND_JOB
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("NO_ANSWER")),
      ),
      "HAVE_A_JOB_AFTER_RELEASE",
      "SUPPORT_TO_FIND_JOB",
    ),
    // If the answer to HELP_CONTACTING_EMPLOYER is YES, go to EMPLOYMENT_DETAILS_BEFORE_CUSTODY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
      ),
      "HELP_CONTACTING_EMPLOYER",
      "EMPLOYMENT_DETAILS_BEFORE_CUSTODY",
    ),
    // If the answer to HELP_CONTACTING_EMPLOYER is NO, go to IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("NO")),
      ),
      "HELP_CONTACTING_EMPLOYER",
      "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
    ),
    // If the answer to HELP_CONTACTING_EMPLOYER is NO_ANSWER, go to IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("NO_ANSWER")),
      ),
      "HELP_CONTACTING_EMPLOYER",
      "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
    ),
    // Any answer to SUPPORT_TO_FIND_JOB, go to IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_TO_FIND_JOB", answer = StringAnswer("NO")),
      ),
      "SUPPORT_TO_FIND_JOB",
      "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
    ),
    // Any answer to EMPLOYMENT_DETAILS_BEFORE_CUSTODY, go to IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
      ),
      "EMPLOYMENT_DETAILS_BEFORE_CUSTODY",
      "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
    ),
    // If the answer to IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY is YES, go to RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("YES")),
      ),
      "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
      "RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE",
    ),
    // If the answer to IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY is NO, go to WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("NO")),
      ),
      "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
      "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
    ),
    // If the answer to IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY is NO_ANSWER, go to WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("NO_ANSWER")),
      ),
      "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
      "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
    ),
    // If the answer to RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE is NO, go to WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("NO")),
      ),
      "RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE",
      "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
    ),
    // If the answer to RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE is YES, go to HELP_CONTACTING_EDUCATION_PROVIDER
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("YES")),
      ),
      "RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE",
      "HELP_CONTACTING_EDUCATION_PROVIDER",
    ),
    // If the answer to RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE is NO_ANSWER, go to HELP_CONTACTING_EDUCATION_PROVIDER
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("NO_ANSWER")),
      ),
      "RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE",
      "HELP_CONTACTING_EDUCATION_PROVIDER",
    ),
    // If the answer to WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE is YES, go to BURSARIES_AND_GRANTS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("YES")),
      ),
      "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
      "BURSARIES_AND_GRANTS",
    ),
    // If the answer to WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE is NO, go to SUPPORT_NEEDS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("NO")),
      ),
      "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
      "ASSESSMENT_SUMMARY",
    ),
    // If the answer to WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE is NO_ANSWER, go to SUPPORT_NEEDS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("NO_ANSWER")),
      ),
      "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
      "ASSESSMENT_SUMMARY",
    ),
    // If the answer to HELP_CONTACTING_EDUCATION_PROVIDER is YES, go to TRAINING_PROVIDER_DETAILS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EDUCATION_PROVIDER", answer = StringAnswer("YES")),
      ),
      "HELP_CONTACTING_EDUCATION_PROVIDER",
      "TRAINING_PROVIDER_DETAILS",
    ),
    // If the answer to HELP_CONTACTING_EDUCATION_PROVIDER is NO, go to BURSARIES_AND_GRANTS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EDUCATION_PROVIDER", answer = StringAnswer("NO")),
      ),
      "HELP_CONTACTING_EDUCATION_PROVIDER",
      "BURSARIES_AND_GRANTS",
    ),
    // If the answer to HELP_CONTACTING_EDUCATION_PROVIDER is NO_ANSWER, go to BURSARIES_AND_GRANTS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EDUCATION_PROVIDER", answer = StringAnswer("NO_ANSWER")),
      ),
      "HELP_CONTACTING_EDUCATION_PROVIDER",
      "BURSARIES_AND_GRANTS",
    ),
    // Any answer to TRAINING_PROVIDER_DETAILS, go to BURSARIES_AND_GRANTS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EDUCATION_PROVIDER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_TRAINING_PROVIDER", answer = StringAnswer("College")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_TRAINING_PROVIDER", answer = MapAnswer(listOf(mapOf("address_line1" to "456 The Street")))),
      ),
      "TRAINING_PROVIDER_DETAILS",
      "BURSARIES_AND_GRANTS",
    ),
    // Any answer to BURSARIES_AND_GRANTS, go to ASSESSMENT_SUMMARY
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EDUCATION_PROVIDER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_TRAINING_PROVIDER", answer = StringAnswer("College")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_TRAINING_PROVIDER", answer = MapAnswer(listOf(mapOf("address_line1" to "456 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("BURSARIES_AND_GRANTS", answer = StringAnswer("YES")),
      ),
      "BURSARIES_AND_GRANTS",
      "ASSESSMENT_SUMMARY",
    ),
    // Any answer to ASSESSMENT_SUMMARY, go to CHECK_ANSWERS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
        ResettlementAssessmentRequestQuestionAndAnswer("JOB_BEFORE_CUSTODY", answer = StringAnswer("NO")),
        ResettlementAssessmentRequestQuestionAndAnswer("HAVE_A_JOB_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EMPLOYER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("EMPLOYMENT_TITLE_BEFORE_CUSTODY", answer = StringAnswer("Chef")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_EMPLOYER", answer = StringAnswer("The Cafe")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_EMPLOYER", answer = MapAnswer(listOf(mapOf("address_line1" to "123 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("HELP_CONTACTING_EDUCATION_PROVIDER", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("NAME_OF_TRAINING_PROVIDER", answer = StringAnswer("College")),
        ResettlementAssessmentRequestQuestionAndAnswer("ADDRESS_OF_TRAINING_PROVIDER", answer = MapAnswer(listOf(mapOf("address_line1" to "456 The Street")))),
        ResettlementAssessmentRequestQuestionAndAnswer("BURSARIES_AND_GRANTS", answer = StringAnswer("YES")),
        ResettlementAssessmentRequestQuestionAndAnswer("SUPPORT_NEEDS", answer = StringAnswer("SUPPORT_REQUIRED")),
        ResettlementAssessmentRequestQuestionAndAnswer("CASE_NOTE_SUMMARY", answer = StringAnswer("Some text here...")),
      ),
      "ASSESSMENT_SUMMARY",
      "CHECK_ANSWERS",
    ),
  )

  override fun `test get page from Id - no existing assessment data`(): Stream<Arguments> = Stream.of(
    Arguments.of(
      "JOB_BEFORE_CUSTODY",
      ResettlementAssessmentResponsePage(
        id = "JOB_BEFORE_CUSTODY",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "JOB_BEFORE_CUSTODY",
              title = "Did the person in prison have a job before custody?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "JOB_BEFORE_CUSTODY",
          ),
        ),
      ),
    ),
    Arguments.of(
      "TYPE_OF_EMPLOYMENT_CONTRACT",
      ResettlementAssessmentResponsePage(
        id = "TYPE_OF_EMPLOYMENT_CONTRACT",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "TYPE_OF_EMPLOYMENT_CONTRACT",
              title = "Type of employment contract",
              subTitle = "Select all that apply.",
              type = TypeOfQuestion.CHECKBOX,
              options = listOf(
                ResettlementAssessmentOption(id = "FULL_TIME_CONTRACT", displayText = "Full-time contract"),
                ResettlementAssessmentOption(id = "PART_TIME_CONTRACT", displayText = "Part-time contract"),
                ResettlementAssessmentOption(id = "PERMANENT_CONTRACT", displayText = "Permanent contract"),
                ResettlementAssessmentOption(id = "TEMPORARY_CONTRACT", displayText = "Temporary contract"),
                ResettlementAssessmentOption(id = "FIXED_TERM_CONTRACT", displayText = "Fixed-term contract"),
                ResettlementAssessmentOption(id = "ZERO_HOURS_CONTRACT", displayText = "Zero hours contract"),
                ResettlementAssessmentOption(id = "NO_ANSWER", displayText = "No answer provided", exclusive = true),
              ),
            ),
            originalPageId = "TYPE_OF_EMPLOYMENT_CONTRACT",
          ),
        ),
      ),
    ),
    Arguments.of(
      "RETURN_TO_JOB_AFTER_RELEASE",
      ResettlementAssessmentResponsePage(
        id = "RETURN_TO_JOB_AFTER_RELEASE",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "RETURN_TO_JOB_AFTER_RELEASE",
              title = "Can the person in prison return to this job after release?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "RETURN_TO_JOB_AFTER_RELEASE",
          ),
        ),
      ),
    ),
    Arguments.of(
      "HAVE_A_JOB_AFTER_RELEASE",
      ResettlementAssessmentResponsePage(
        id = "HAVE_A_JOB_AFTER_RELEASE",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "HAVE_A_JOB_AFTER_RELEASE",
              title = "Does the person in prison have a job when they are released?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "HAVE_A_JOB_AFTER_RELEASE",
          ),
        ),
      ),
    ),
    Arguments.of(
      "HELP_CONTACTING_EMPLOYER",
      ResettlementAssessmentResponsePage(
        id = "HELP_CONTACTING_EMPLOYER",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "HELP_CONTACTING_EMPLOYER",
              title = "Does the person in prison need help contacting the employer?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "HELP_CONTACTING_EMPLOYER",
          ),
        ),
      ),
    ),
    Arguments.of(
      "EMPLOYMENT_DETAILS_BEFORE_CUSTODY",
      ResettlementAssessmentResponsePage(
        id = "EMPLOYMENT_DETAILS_BEFORE_CUSTODY",
        title = "Employment before custody",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "EMPLOYMENT_TITLE_BEFORE_CUSTODY",
              title = "Job title",
              type = TypeOfQuestion.SHORT_TEXT,
            ),
            originalPageId = "EMPLOYMENT_DETAILS_BEFORE_CUSTODY",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "NAME_OF_EMPLOYER",
              title = "Employer",
              type = TypeOfQuestion.SHORT_TEXT,
            ),
            originalPageId = "EMPLOYMENT_DETAILS_BEFORE_CUSTODY",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "ADDRESS_OF_EMPLOYER",
              title = "Employer address",
              type = TypeOfQuestion.ADDRESS,
            ),
            originalPageId = "EMPLOYMENT_DETAILS_BEFORE_CUSTODY",
          ),
        ),
      ),
    ),
    Arguments.of(
      "SUPPORT_TO_FIND_JOB",
      ResettlementAssessmentResponsePage(
        id = "SUPPORT_TO_FIND_JOB",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_TO_FIND_JOB",
              title = "Does the person in prison want support to find a job when they are released?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "SUPPORT_TO_FIND_JOB",
          ),
        ),
      ),
    ),
    Arguments.of(
      "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
      ResettlementAssessmentResponsePage(
        id = "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
              title = "Was the person in prison in education or training before custody?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY",
          ),
        ),
      ),
    ),
    Arguments.of(
      "RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE",
      ResettlementAssessmentResponsePage(
        id = "RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE",
              title = "Can the person in prison return to this education or training after release?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "RETURN_TO_EDUCATION_OR_TRAINING_AFTER_RELEASE",
          ),
        ),
      ),
    ),
    Arguments.of(
      "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
      ResettlementAssessmentResponsePage(
        id = "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
              title = "Does the person in prison want to start education or training after release?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE",
          ),
        ),
      ),
    ),
    Arguments.of(
      "HELP_CONTACTING_EDUCATION_PROVIDER",
      ResettlementAssessmentResponsePage(
        id = "HELP_CONTACTING_EDUCATION_PROVIDER",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "HELP_CONTACTING_EDUCATION_PROVIDER",
              title = "Does the person in prison want help contacting an education or training provider?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "HELP_CONTACTING_EDUCATION_PROVIDER",
          ),
        ),
      ),
    ),
    Arguments.of(
      "TRAINING_PROVIDER_DETAILS",
      ResettlementAssessmentResponsePage(
        id = "TRAINING_PROVIDER_DETAILS",
        title = "Education or training before custody",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "NAME_OF_TRAINING_PROVIDER",
              title = "Education or training provider",
              type = TypeOfQuestion.SHORT_TEXT,
            ),
            originalPageId = "TRAINING_PROVIDER_DETAILS",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "ADDRESS_OF_TRAINING_PROVIDER",
              title = "Education or training provider address",
              type = TypeOfQuestion.ADDRESS,
            ),
            originalPageId = "TRAINING_PROVIDER_DETAILS",
          ),
        ),
      ),
    ),
    Arguments.of(
      "BURSARIES_AND_GRANTS",
      ResettlementAssessmentResponsePage(
        id = "BURSARIES_AND_GRANTS",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "BURSARIES_AND_GRANTS",
              title = "Does the person in prison want to find out about bursaries and grants for courses or training?",
              type = TypeOfQuestion.RADIO,
              options = yesNoOptions,
            ),
            originalPageId = "BURSARIES_AND_GRANTS",
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
              title = "Education, skills and work support needs",
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
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "CASE_NOTE_SUMMARY",
              title = "Case note summary",
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
        questionsAndAnswers = listOf(),
      ),
    ),
  )
}
