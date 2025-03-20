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
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.TagAndQuestionMapping
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.Answer
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

class AccommodationV4ResettlementAssessmentAssessmentStrategyTest : BaseResettlementAssessmentStrategyTest(Pathway.ACCOMMODATION, 4) {

  override fun `test next page function flow - no existing assessment data`(): Stream<Arguments> = Stream.of(
    // Start of flow - send null current page to get first page
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      null,
      "ACCOMMODATION_REPORT",
    ),
    // Any answer to ACCOMMODATION_REPORT, go to CHECK_ANSWERS
    Arguments.of(
      listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(),
      "ACCOMMODATION_REPORT",
      "CHECK_ANSWERS",
    ),
  )

  override fun `test get page from Id - no existing assessment data`(): Stream<Arguments> = Stream.of(
    Arguments.of(
      "ACCOMMODATION_REPORT",
      getExpectedV4AccommodationReportPage(),
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
    val pathway = Pathway.ACCOMMODATION

    val prisonerEntity = PrisonerEntity(1, nomsId, testDate, "ABC")

    Mockito.lenient().`when`(prisonerRepository.findByNomsId(nomsId)).thenReturn(prisonerEntity)

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
      resettlementAssessmentStrategy.completeAssessment(nomsId, pathway, assessmentType, assessment, "string", false)
      Mockito.verify(resettlementAssessmentRepository).save(expectedEntity!!)
    } else {
      val actualException = assertThrows<Throwable> {
        resettlementAssessmentStrategy.completeAssessment(nomsId, pathway, assessmentType, assessment, "string", false)
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
        version = version,
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
        ),
      ),
      ResettlementAssessmentEntity(id = null, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = null, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "PRIVATE_RENTED_HOUSING")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING", answer = StringAnswer(answer = "RETURN_TO_PREVIOUS_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Some additional details")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "MOVE_TO_NEW_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS", answer = StringAnswer(answer = "Some more information here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Some other additional details")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = null, createdByUserId = "USER_1", version = version, submissionDate = null, userDeclaration = false),
      null,
      null,
      null,
    ),
    // Happy path - RESETTLEMENT_PLAN and no existing assessment
    Arguments.of(
      ResettlementAssessmentType.RESETTLEMENT_PLAN,
      ResettlementAssessmentCompleteRequest(
        version = version,
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
        ),
      ),
      ResettlementAssessmentEntity(id = null, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = null, assessmentType = ResettlementAssessmentType.RESETTLEMENT_PLAN, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "PRIVATE_RENTED_HOUSING")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING", answer = StringAnswer(answer = "RETURN_TO_PREVIOUS_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Some additional details")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "MOVE_TO_NEW_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS", answer = StringAnswer(answer = "Some more information here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Some other additional details")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = null, createdByUserId = "USER_1", version = version, submissionDate = null, userDeclaration = false),
      null,
      null,
      null,
    ),
    // Happy path - existing COMPLETE assessment
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      ResettlementAssessmentCompleteRequest(
        version = version,
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
        ),
      ),
      ResettlementAssessmentEntity(id = null, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = null, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING", answer = StringAnswer(answer = "RETURN_TO_PREVIOUS_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Some additional details")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "MOVE_TO_NEW_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS", answer = StringAnswer(answer = "Some more information here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Some other additional details")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = null, createdByUserId = "USER_1", version = version, submissionDate = null, userDeclaration = false),
      null,
      ResettlementAssessmentEntity(id = 12, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = null, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING", answer = StringAnswer(answer = "RETURN_TO_PREVIOUS_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "MOVE_TO_NEW_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS", answer = StringAnswer(answer = "Some more information here")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = null, createdByUserId = "USER_1", version = version, submissionDate = null, userDeclaration = false),
      null,
    ),
    // Happy path - existing SUBMITTED assessment
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      ResettlementAssessmentCompleteRequest(
        version = version,
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
        ),
      ),
      ResettlementAssessmentEntity(id = null, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = null, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING", answer = StringAnswer(answer = "RETURN_TO_PREVIOUS_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Some additional details")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "MOVE_TO_NEW_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS", answer = StringAnswer(answer = "Some more information here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Some other additional details")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = null, createdByUserId = "USER_1", version = version, submissionDate = null, userDeclaration = false),
      null,
      ResettlementAssessmentEntity(id = 12, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = null, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING", answer = StringAnswer(answer = "RETURN_TO_PREVIOUS_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "MOVE_TO_NEW_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS", answer = StringAnswer(answer = "Some more information here")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = null, createdByUserId = "USER_1", version = version, submissionDate = null, userDeclaration = false),
      ProfileTagsEntity(id = null, prisonerId = 1, ProfileTagList(listOf(TagAndQuestionMapping.NO_FIXED_ABODE.name)), LocalDateTime.parse("2023-08-16T12:00")),
    ),
    // Happy path - existing SUBMITTED assessment with Tags generated for multiple answers
    Arguments.of(
      ResettlementAssessmentType.BCST2,
      ResettlementAssessmentCompleteRequest(
        version = version,
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
        ),
      ),
      ResettlementAssessmentEntity(id = null, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = null, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING", answer = StringAnswer(answer = "RETURN_TO_PREVIOUS_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Some additional details")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "MOVE_TO_NEW_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS", answer = StringAnswer(answer = "Some more information here")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDITIONAL_DETAILS", answer = StringAnswer(answer = "Some other additional details")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = null, createdByUserId = "USER_1", version = version, submissionDate = null, userDeclaration = false),
      null,
      ResettlementAssessmentEntity(id = 12, prisonerId = 1, pathway = Pathway.ACCOMMODATION, statusChangedTo = null, assessmentType = ResettlementAssessmentType.BCST2, assessment = ResettlementAssessmentQuestionAndAnswerList(assessment = listOf(ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE", answer = StringAnswer(answer = "NO_PERMANENT_OR_FIXED")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_DID_THEY_LIVE_ADDRESS_PRIVATE_RENTED_HOUSING", answer = StringAnswer(answer = "RETURN_TO_PREVIOUS_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE", answer = StringAnswer(answer = "MOVE_TO_NEW_ADDRESS")), ResettlementAssessmentSimpleQuestionAndAnswer(questionId = "WHERE_WILL_THEY_LIVE_ADDRESS_MOVE_TO_NEW_ADDRESS", answer = StringAnswer(answer = "Some more information here")))), creationDate = LocalDateTime.parse("2023-08-16T12:00:00"), createdBy = "System user", assessmentStatus = ResettlementAssessmentStatus.COMPLETE, caseNoteText = null, createdByUserId = "USER_1", version = version, submissionDate = null, userDeclaration = false),
      ProfileTagsEntity(id = null, prisonerId = 1, ProfileTagList(listOf(TagAndQuestionMapping.NO_FIXED_ABODE.name, TagAndQuestionMapping.HOME_ADAPTATIONS_POST_RELEASE.name, TagAndQuestionMapping.KEEP_THEIR_HOME.name, TagAndQuestionMapping.CANCEL_TENANCY.name)), LocalDateTime.parse("2023-08-16T12:00")),
    ),
  )
}

fun getExpectedV4AccommodationReportPage(answers: Map<String, Answer<*>>? = null) = ResettlementAssessmentResponsePage(
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
                  validationType = ValidationType.OPTIONAL,
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
                  validationType = ValidationType.OPTIONAL,
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
                  validationType = ValidationType.OPTIONAL,
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
                  validationType = ValidationType.OPTIONAL,
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
                  validationType = ValidationType.OPTIONAL,
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
                  validationType = ValidationType.OPTIONAL,
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
                  validationType = ValidationType.OPTIONAL,
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
                  validationType = ValidationType.OPTIONAL,
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
                  validationType = ValidationType.OPTIONAL,
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
                  validationType = ValidationType.OPTIONAL,
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
