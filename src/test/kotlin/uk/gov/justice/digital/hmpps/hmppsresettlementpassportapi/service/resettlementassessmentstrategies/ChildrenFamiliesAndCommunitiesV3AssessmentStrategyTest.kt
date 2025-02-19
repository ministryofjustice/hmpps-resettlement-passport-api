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

class ChildrenFamiliesAndCommunitiesV3AssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.CHILDREN_FAMILIES_AND_COMMUNITY, 3) {

  override fun `test next page function flow - no existing assessment data`(): Stream<Arguments> = Stream.of(
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

  override fun `test get page from Id - no existing assessment data`(): Stream<Arguments> = Stream.of(
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
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison has a partner or spouse"),
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "PARTNER_OR_SPOUSE_ADDITIONAL_DETAILS",
              title = "Additional details",
              subTitle = "Include their full name and date of birth.",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "PARENTAL_RESPONSIBILITY",
              title = "Does the person in prison have parental responsibility for any children under 16?",
              subTitle = "Parental responsibility means they have legal rights and duties relating to the children's upbringing. It does not mean they are allowed contact.",
              type = TypeOfQuestion.RADIO,
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison has parental responsibility for any children under 16"),
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "PARENTAL_RESPONSIBILITY_ADDITIONAL_DETAILS",
              title = "Additional details",
              subTitle = "Include their names, whether they are with current or ex partner and their dates of birth. Specify how many children they are the primary carer for, and where they are while they are in custody. Specify if social services are involved, including name of social worker.",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "CARING_RESPONSIBILITIES_FOR_ADULTS",
              title = "Does the person in prison have caring responsibilities for any adults?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison has caring responsibilities for any adults"),
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "CARING_RESPONSIBILITIES_FOR_ADULTS_ADDITIONAL_DETAILS",
              title = "Additional details",
              subTitle = "Details of any adults they have caring responsibilities for. Specify if social services are involved, including name of social worker.",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "RECEIVED_SUPPORT_FROM_SOCIAL_SERVICES",
              title = "Has the person in prison ever received support from social services or been in care?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison has ever received support from social services or been in care"),
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "RECEIVED_SUPPORT_FROM_SOCIAL_SERVICES_ADDITIONAL_DETAILS",
              title = "Additional details",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "DO_THEY_HAVE_SUPPORT_FROM_FAMILY_FRIENDS_COMMUNITY",
              title = "Does the person in prison have support from family, friends or their community outside of prison?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison has support from family, friends or their community outside of prison"),
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "DO_THEY_HAVE_SUPPORT_FROM_FAMILY_FRIENDS_COMMUNITY_ADDITIONAL_DETAILS",
              title = "Additional details",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "INVOLVEMENT_IN_GANG_ACTIVITY",
              title = "Has the person in prison had any involvement in gang activity?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison has had any involvement in gang activity"),
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "INVOLVEMENT_IN_GANG_ACTIVITY_ADDITIONAL_DETAILS",
              title = "Additional details",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "UNDER_THREAT_OUTSIDE_PRISON",
              title = "Is the person in prison under threat outside of prison?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison is under threat outside of prison"),
              options = yesNoOptions,
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "UNDER_THREAT_OUTSIDE_PRISON_ADDITIONAL_DETAILS",
              title = "Additional details",
              type = TypeOfQuestion.LONG_TEXT,
              validationType = ValidationType.OPTIONAL,
              validation = Validation(ValidationType.OPTIONAL),
            ),
            originalPageId = "CHILDREN_FAMILIES_AND_COMMUNITY_REPORT",
          ),
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "VICTIM_OF_DOMESTIC_ABUSE",
              title = "Has the person in prison ever been the victim of domestic abuse?",
              subTitle = null,
              type = TypeOfQuestion.RADIO,
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison has ever been the victim of domestic abuse"),
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
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison has ever been the perpetrator of domestic abuse"),
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
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison has ever been the victim of sexual abuse"),
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
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison has ever been the perpetrator of sexual abuse"),
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
              validation = Validation(ValidationType.MANDATORY, message = "Select whether the person in prison has ever worked in the sex industry"),
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
              validation = Validation(ValidationType.MANDATORY, message = "Select support needs or select 'No support needs identified'"),
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
        title = "Children, families and communities report summary",
        questionsAndAnswers = listOf(
          ResettlementAssessmentQuestionAndAnswer(
            question = ResettlementAssessmentQuestion(
              id = "SUPPORT_NEEDS",
              title = "Children, families and communities resettlement status",
              subTitle = "Select one option.",
              type = TypeOfQuestion.RADIO,
              validation = Validation(ValidationType.MANDATORY, message = "Select a children, families and communities resettlement status"),
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
