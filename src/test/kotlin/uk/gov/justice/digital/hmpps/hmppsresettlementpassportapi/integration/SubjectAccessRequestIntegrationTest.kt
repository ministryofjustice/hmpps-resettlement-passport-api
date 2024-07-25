package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate

class SubjectAccessRequestIntegrationTest : IntegrationTestBase() {
  private final val expectedPrisoner = "prisoner={id=1, nomsId=G1458GV, creationDate=2024-01-01T12:21:38.709, crn=ABC123, prisonId=null, releaseDate=null}"
  private final val expectedAssessment = "assessment={id=1, prisonerId=1, creationDate=2023-08-16T12:21:38.709, " +
    "assessmentDate=2023-08-16T12:21:38.709, isBankAccountRequired=true, isIdRequired=true, idDocuments=[], isDeleted=false, deletionDate=null}"
  private final val expectedBankApplication = "bankApplication={id=1, $expectedPrisoner, logs=[], applicationSubmittedDate=2023-08-16T12:21:38.709, " +
    "currentStatus=Application submitted, bankName=Nationwide, bankResponseDate=null, isAddedToPersonalItems=false, addedToPersonalItemsDate=null}"
  private final val expectedDeliusContact = "deliusContact=[{caseNoteId=db-1, pathway=ACCOMMODATION, creationDateTime=2023-08-16T12:21:38.709, " +
    "occurenceDateTime=2023-08-16T12:21:38.709, createdBy=username1, text=Some notes here}, " +
    "{caseNoteId=db-2, pathway=HEALTH, creationDateTime=2024-06-01T12:21:38.709, occurenceDateTime=2024-06-01T12:21:38.709, createdBy=username1, text=health notes}]"
  private final val expectedIdApplication = "idApplication={id=1, " +
    "prisonerId=1, idType={id=2, name=Marriage certificate}, creationDate=2023-08-16T12:21:38.709, applicationSubmittedDate=2023-08-16T12:21:38.709, " +
    "isPriorityApplication=false, costOfApplication=10, refundAmount=null, haveGro=false, isUkNationalBornOverseas=false, countryBornIn=null, " +
    "caseNumber=null, courtDetails=null, driversLicenceType=null, driversLicenceApplicationMadeAt=null, isAddedToPersonalItems=false, " +
    "addedToPersonalItemsDate=null, status=pending, statusUpdateDate=2023-08-16T12:21:38.709, isDeleted=false, deletionDate=null, dateIdReceived=null}"
  private final val expectedStatusSummary = "statusSummary=[{type=BCST2, " +
    "pathwayStatus=[{pathway=ACCOMMODATION, assessmentStatus=SUBMITTED}, " +
    "{pathway=ATTITUDES_THINKING_AND_BEHAVIOUR, assessmentStatus=SUBMITTED}, " +
    "{pathway=CHILDREN_FAMILIES_AND_COMMUNITY, assessmentStatus=SUBMITTED}, " +
    "{pathway=DRUGS_AND_ALCOHOL, assessmentStatus=SUBMITTED}, " +
    "{pathway=EDUCATION_SKILLS_AND_WORK, assessmentStatus=SUBMITTED}, " +
    "{pathway=FINANCE_AND_ID, assessmentStatus=SUBMITTED}, " +
    "{pathway=HEALTH, assessmentStatus=SUBMITTED}]}, " +
    "{type=RESETTLEMENT_PLAN, " +
    "pathwayStatus=[{pathway=ACCOMMODATION, assessmentStatus=SUBMITTED}, " +
    "{pathway=ATTITUDES_THINKING_AND_BEHAVIOUR, assessmentStatus=SUBMITTED}, " +
    "{pathway=CHILDREN_FAMILIES_AND_COMMUNITY, assessmentStatus=SUBMITTED}, " +
    "{pathway=DRUGS_AND_ALCOHOL, assessmentStatus=SUBMITTED}, " +
    "{pathway=EDUCATION_SKILLS_AND_WORK, assessmentStatus=SUBMITTED}, " +
    "{pathway=FINANCE_AND_ID, assessmentStatus=SUBMITTED}, " +
    "{pathway=HEALTH, assessmentStatus=SUBMITTED}]}]"
  private final val expectedResettlementAssessment = "resettlementAssessment=[{originalAssessment={assessmentType=BCST2, " +
    "lastUpdated=2024-05-22T13:40:13.933994, updatedBy=Matthew Kerry, " +
    "questionsAndAnswers=[{questionTitle=Where did the person in prison live before custody?, " +
    "answer=Social housing, originalPageId=WHERE_DID_THEY_LIVE}, {questionTitle=Enter the address, answer=A Road" +
    "Some Town" +
    "Yorkshire" +
    "AB13 C45, originalPageId=WHERE_DID_THEY_LIVE_ADDRESS}, " +
    "{questionTitle=Does the person in prison or their family need help to keep their home while they are in prison?, answer=No answer provided, originalPageId=HELP_TO_KEEP_HOME}, " +
    "{questionTitle=Where will the person in prison live when they are released?, answer=No answer provided, originalPageId=WHERE_WILL_THEY_LIVE_1}]}, " +
    "latestAssessment={assessmentType=RESETTLEMENT_PLAN, lastUpdated=2024-05-22T17:02:41.310279, updatedBy=Matthew Kerry, " +
    "questionsAndAnswers=[{questionTitle=Where did the person in prison live before custody?, answer=Social housing, originalPageId=WHERE_DID_THEY_LIVE}, " +
    "{questionTitle=Enter the address, answer=A Road" +
    "Some Town" +
    "Yorkshire" +
    "AB13 C45, originalPageId=WHERE_DID_THEY_LIVE_ADDRESS}, " +
    "{questionTitle=Does the person in prison or their family need help to keep their home while they are in prison?, answer=No answer provided, originalPageId=HELP_TO_KEEP_HOME}, " +
    "{questionTitle=Where will the person in prison live when they are released?, answer=No answer provided, originalPageId=WHERE_WILL_THEY_LIVE_1}]}}, " +
    "{originalAssessment={assessmentType=BCST2, lastUpdated=2024-05-22T16:45:51.383575, updatedBy=Matthew Kerry, " +
    "questionsAndAnswers=[{questionTitle=Does the person in prison want support managing their emotions?, answer=No, originalPageId=HELP_TO_MANAGE_ANGER}, " +
    "{questionTitle=Does the person in prison want support with gambling issues?, answer=No, originalPageId=ISSUES_WITH_GAMBLING}]}, " +
    "latestAssessment={assessmentType=RESETTLEMENT_PLAN, lastUpdated=2024-05-22T17:05:17.294788, updatedBy=Matthew Kerry, " +
    "questionsAndAnswers=[{questionTitle=Does the person in prison want support managing their emotions?, answer=No, originalPageId=HELP_TO_MANAGE_ANGER}, " +
    "{questionTitle=Does the person in prison want support with gambling issues?, answer=No, originalPageId=ISSUES_WITH_GAMBLING}]}}, " +
    "{originalAssessment={assessmentType=BCST2, lastUpdated=2024-05-22T16:46:17.002205, updatedBy=Matthew Kerry, " +
    "questionsAndAnswers=[{questionTitle=Does the person in prison have a partner or spouse?, answer=No answer provided, originalPageId=PARTNER_OR_SPOUSE}, " +
    "{questionTitle=Is the person in prison the primary carer for any children?, answer=No, originalPageId=PRIMARY_CARER_FOR_CHILDREN}, " +
    "{questionTitle=Does the person in prison have caring responsibilities for any adults?, answer=No answer provided, originalPageId=CARING_FOR_ADULT}, " +
    "{questionTitle=Has the person in prison themselves ever received support from social services?, answer=No answer provided, originalPageId=SUPPORT_FROM_SOCIAL_SERVICES}, " +
    "{questionTitle=Will the person in prison have support from family, friends or their community outside of prison?, answer=No answer provided, originalPageId=FRIEND_FAMILY_COMMUNITY_SUPPORT}, " +
    "{questionTitle=Has the person in prison had any involvement in gang activity?, answer=No answer provided, originalPageId=INVOLVEMENT_IN_GANG_ACTIVITY}, " +
    "{questionTitle=Is the person in prison under threat outside of prison?, answer=No answer provided, originalPageId=UNDER_THREAT_OUTSIDE}, " +
    "{questionTitle=Does the person in prison need support from community organisations outside of prison?, answer=No answer provided, originalPageId=COMMUNITY_ORGANISATION_SUPPORT}]}, " +
    "latestAssessment={assessmentType=RESETTLEMENT_PLAN, lastUpdated=2024-05-22T17:05:40.499062, updatedBy=Matthew Kerry, " +
    "questionsAndAnswers=[{questionTitle=Does the person in prison have a partner or spouse?, answer=No answer provided, originalPageId=PARTNER_OR_SPOUSE}, " +
    "{questionTitle=Is the person in prison the primary carer for any children?, answer=No, originalPageId=PRIMARY_CARER_FOR_CHILDREN}, " +
    "{questionTitle=Does the person in prison have caring responsibilities for any adults?, answer=No answer provided, originalPageId=CARING_FOR_ADULT}, " +
    "{questionTitle=Has the person in prison themselves ever received support from social services?, answer=No answer provided, originalPageId=SUPPORT_FROM_SOCIAL_SERVICES}, " +
    "{questionTitle=Will the person in prison have support from family, friends or their community outside of prison?, answer=No answer provided, originalPageId=FRIEND_FAMILY_COMMUNITY_SUPPORT}, " +
    "{questionTitle=Has the person in prison had any involvement in gang activity?, answer=No answer provided, originalPageId=INVOLVEMENT_IN_GANG_ACTIVITY}, " +
    "{questionTitle=Is the person in prison under threat outside of prison?, answer=No answer provided, originalPageId=UNDER_THREAT_OUTSIDE}, " +
    "{questionTitle=Does the person in prison need support from community organisations outside of prison?, answer=No answer provided, originalPageId=COMMUNITY_ORGANISATION_SUPPORT}]}}, " +
    "{originalAssessment={assessmentType=BCST2, lastUpdated=2024-05-22T16:46:31.837468, updatedBy=Matthew Kerry, " +
    "questionsAndAnswers=[{questionTitle=Does the person in prison have any previous or current drug misuse issues?, answer=No, originalPageId=DRUG_ISSUES}, " +
    "{questionTitle=Does the person in prison have any previous or current alcohol misuse issues?, answer=No, originalPageId=ALCOHOL_ISSUES}]}, " +
    "latestAssessment={assessmentType=RESETTLEMENT_PLAN, lastUpdated=2024-05-22T17:05:57.32981, updatedBy=Matthew Kerry, " +
    "questionsAndAnswers=[{questionTitle=Does the person in prison have any previous or current drug misuse issues?, answer=Yes, originalPageId=DRUG_ISSUES}, " +
    "{questionTitle=Does the person in prison want support with drug issues from the drug and alcohol team to help them prepare for release?, answer=No, originalPageId=SUPPORT_WITH_DRUG_ISSUES}, " +
    "{questionTitle=Does the person in prison have any previous or current alcohol misuse issues?, answer=No, originalPageId=ALCOHOL_ISSUES}]}}, " +
    "{originalAssessment={assessmentType=BCST2, lastUpdated=2024-05-22T16:46:50.480459, updatedBy=Matthew Kerry, " +
    "questionsAndAnswers=[{questionTitle=Did the person in prison have a job before custody?, answer=No answer provided, originalPageId=JOB_BEFORE_CUSTODY}, " +
    "{questionTitle=Does the person in prison have a job when they are released?, answer=No, originalPageId=HAVE_A_JOB_AFTER_RELEASE}, " +
    "{questionTitle=Does the person in prison want support to find a job when they are released?, answer=No answer provided, originalPageId=SUPPORT_TO_FIND_JOB}, " +
    "{questionTitle=Was the person in prison in education or training before custody?, answer=No answer provided, originalPageId=IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY}, " +
    "{questionTitle=Does the person in prison want to start education or training after release?, answer=No, originalPageId=WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE}]}, " +
    "latestAssessment={assessmentType=RESETTLEMENT_PLAN, lastUpdated=2024-05-22T17:06:14.28783, updatedBy=Matthew Kerry, " +
    "questionsAndAnswers=[{questionTitle=Did the person in prison have a job before custody?, answer=No answer provided, originalPageId=JOB_BEFORE_CUSTODY}, " +
    "{questionTitle=Does the person in prison have a job when they are released?, answer=No, originalPageId=HAVE_A_JOB_AFTER_RELEASE}, " +
    "{questionTitle=Does the person in prison want support to find a job when they are released?, answer=No answer provided, originalPageId=SUPPORT_TO_FIND_JOB}, " +
    "{questionTitle=Was the person in prison in education or training before custody?, answer=No answer provided, originalPageId=IN_EDUCATION_OR_TRAINING_BEFORE_CUSTODY}, " +
    "{questionTitle=Does the person in prison want to start education or training after release?, answer=No, originalPageId=WANT_TO_START_EDUCATION_OR_TRAINING_AFTER_RELEASE}]}}, " +
    "{originalAssessment={assessmentType=BCST2, lastUpdated=2024-05-22T16:47:14.182755, updatedBy=Matthew Kerry, " +
    "questionsAndAnswers=[{questionTitle=Does the person in prison have a bank account?, answer=No answer provided, originalPageId=HAS_BANK_ACCOUNT}, " +
    "{questionTitle=Does the person in prison want help to apply for a bank account?, answer=No, originalPageId=HELP_WITH_BANK_ACCOUNT}, " +
    "{questionTitle=What ID documents does the person in prison have?, answer=Birth or adoption certificate" +
    "Passport, originalPageId=WHAT_ID_DOCUMENTS}, " +
    "{questionTitle=Does the person leaving prison want help to apply for ID?, answer=No answer provided, originalPageId=HELP_APPLY_FOR_ID}, " +
    "{questionTitle=Was the person in prison receiving benefits before custody?, answer=No answer provided, originalPageId=RECEIVING_BENEFITS}, " +
    "{questionTitle=Does the person in prison have any debts or arrears?, answer=No answer provided, originalPageId=DEBTS_OR_ARREARS}]}, " +
    "latestAssessment={assessmentType=RESETTLEMENT_PLAN, lastUpdated=2024-05-22T17:06:32.454374, updatedBy=Matthew Kerry, " +
    "questionsAndAnswers=[{questionTitle=Does the person in prison have a bank account?, answer=No answer provided, originalPageId=HAS_BANK_ACCOUNT}, " +
    "{questionTitle=Does the person in prison want help to apply for a bank account?, answer=No, originalPageId=HELP_WITH_BANK_ACCOUNT}, " +
    "{questionTitle=What ID documents does the person in prison have?, answer=Birth or adoption certificate" +
    "Passport, originalPageId=WHAT_ID_DOCUMENTS}, " +
    "{questionTitle=Does the person leaving prison want help to apply for ID?, answer=No answer provided, originalPageId=HELP_APPLY_FOR_ID}, " +
    "{questionTitle=Was the person in prison receiving benefits before custody?, answer=No answer provided, originalPageId=RECEIVING_BENEFITS}, " +
    "{questionTitle=Does the person in prison have any debts or arrears?, answer=No answer provided, originalPageId=DEBTS_OR_ARREARS}]}}, " +
    "{originalAssessment={assessmentType=BCST2, lastUpdated=2024-05-22T16:47:28.145149, updatedBy=Matthew Kerry, " +
    "questionsAndAnswers=[{questionTitle=Is the person in prison registered with a GP surgery outside of prison?, answer=Yes, originalPageId=REGISTERED_WITH_GP}, " +
    "{questionTitle=Does the person in prison want to meet with a prison healthcare team?, answer=No, originalPageId=MEET_HEALTHCARE_TEAM}]}, " +
    "latestAssessment={assessmentType=RESETTLEMENT_PLAN, lastUpdated=2024-05-22T17:06:48.238178, updatedBy=Matthew Kerry, " +
    "questionsAndAnswers=[{questionTitle=Is the person in prison registered with a GP surgery outside of prison?, answer=Yes, originalPageId=REGISTERED_WITH_GP}, " +
    "{questionTitle=Does the person in prison want to meet with a prison healthcare team?, answer=No, originalPageId=MEET_HEALTHCARE_TEAM}]}}]"

  @Test
  @Sql("classpath:testdata/sql/seed-sar-data-1.sql")
  fun `SAR without Dates`() {
    val nomsId = "G1458GV"

    val response = webTestClient.get()
      .uri("/subject-access-request?prn=$nomsId")
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody(HmppsSubjectAccessRequestContent::class.java)
      .returnResult().responseBody

    val responseContent = response?.content.toString().replace("\n", "")
    assertThat(responseContent).contains(expectedPrisoner)
    assertThat(responseContent).contains(expectedAssessment)
    assertThat(responseContent).contains(expectedBankApplication)
    assertThat(responseContent).contains(expectedDeliusContact)
    assertThat(responseContent).contains(expectedIdApplication)
    assertThat(responseContent).contains(expectedStatusSummary)
    assertThat(responseContent).contains(expectedResettlementAssessment)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-sar-data-1.sql")
  fun `SAR with Dates`() {
    val yesterdayExpectedAssessment = "assessment=null"
    val yesterdayExpectedBankApplication = "bankApplication=null"
    val yesterdayExpectedDeliusContact = "deliusContact=[]"
    val yesterdayExpectedIdApplication = "idApplication=null"
    val yesterdayExpectedStatsuSummary = "statusSummary=[{type=BCST2, " +
      "pathwayStatus=[{pathway=ACCOMMODATION, assessmentStatus=NOT_STARTED}, " +
      "{pathway=ATTITUDES_THINKING_AND_BEHAVIOUR, assessmentStatus=NOT_STARTED}, " +
      "{pathway=CHILDREN_FAMILIES_AND_COMMUNITY, assessmentStatus=NOT_STARTED}, " +
      "{pathway=DRUGS_AND_ALCOHOL, assessmentStatus=NOT_STARTED}, " +
      "{pathway=EDUCATION_SKILLS_AND_WORK, assessmentStatus=NOT_STARTED}, " +
      "{pathway=FINANCE_AND_ID, assessmentStatus=NOT_STARTED}, " +
      "{pathway=HEALTH, assessmentStatus=NOT_STARTED}]}, " +
      "{type=RESETTLEMENT_PLAN, " +
      "pathwayStatus=[{pathway=ACCOMMODATION, assessmentStatus=NOT_STARTED}, " +
      "{pathway=ATTITUDES_THINKING_AND_BEHAVIOUR, assessmentStatus=NOT_STARTED}, " +
      "{pathway=CHILDREN_FAMILIES_AND_COMMUNITY, assessmentStatus=NOT_STARTED}, " +
      "{pathway=DRUGS_AND_ALCOHOL, assessmentStatus=NOT_STARTED}, " +
      "{pathway=EDUCATION_SKILLS_AND_WORK, assessmentStatus=NOT_STARTED}, " +
      "{pathway=FINANCE_AND_ID, assessmentStatus=NOT_STARTED}, " +
      "{pathway=HEALTH, assessmentStatus=NOT_STARTED}]}]"
    val yesterdayExpectedResettlementAssessment = "resettlementAssessment=[]"

    val nomsId = "G1458GV"
    val fromYesterday = LocalDate.now().minusDays(1)
    val fromLastYear = LocalDate.now().minusYears(1)

    val responseYesterday = webTestClient.get()
      .uri("/subject-access-request?prn=$nomsId&fromDate=$fromYesterday")
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody(HmppsSubjectAccessRequestContent::class.java)
      .returnResult().responseBody

    val responseContentYesterday = responseYesterday?.content.toString().replace("\n", "")
    assertThat(responseContentYesterday).contains(expectedPrisoner)
    assertThat(responseContentYesterday).contains(yesterdayExpectedAssessment)
    assertThat(responseContentYesterday).contains(yesterdayExpectedBankApplication)
    assertThat(responseContentYesterday).contains(yesterdayExpectedDeliusContact)
    assertThat(responseContentYesterday).contains(yesterdayExpectedIdApplication)
    assertThat(responseContentYesterday).contains(yesterdayExpectedStatsuSummary)
    assertThat(responseContentYesterday).contains(yesterdayExpectedResettlementAssessment)

    val response = webTestClient.get()
      .uri("/subject-access-request?prn=$nomsId&fromDate=$fromLastYear")
      .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody(HmppsSubjectAccessRequestContent::class.java)
      .returnResult().responseBody

    val responseContent = response?.content.toString().replace("\n", "")
    assertThat(responseContent).contains(expectedPrisoner)
    assertThat(responseContent).contains(expectedAssessment)
    assertThat(responseContent).contains(expectedBankApplication)
    assertThat(responseContent).contains(expectedDeliusContact)
    assertThat(responseContent).contains(expectedIdApplication)
    assertThat(responseContent).contains(expectedStatusSummary)
    assertThat(responseContent).contains(expectedResettlementAssessment)
  }
}
