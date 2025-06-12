package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.resettlementassessment.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service.FeatureFlagValueProvider

@TestConfiguration
class ResettlementAssessmentTestMockConfig {
  @Bean
  @Primary
  fun featureFlagValueProvider(): FeatureFlagValueProvider = mockk { every { isReadOnlyMode() } returns true }
}

@Import(ResettlementAssessmentTestMockConfig::class)
class ResettlementAssessmentIntegrationReadOnlyModeTest : IntegrationTestBase() {

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-4.sql")
  fun `Post resettlement assessment submit - forbidden`() {
    val nomsId = "ABC1234"
    val assessmentType = "BCST2"

    webTestClient.post()
      .uri("resettlement-passport/prisoner/$nomsId/resettlement-assessment/submit?assessmentType=$assessmentType")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), authSource = "nomis"))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-3.sql")
  fun `Post resettlement assessment complete - forbidden`() {
    val nomsId = "ABC1234"
    val pathway = "ACCOMMODATION"
    val assessmentType = "BCST2"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/complete?assessmentType=$assessmentType&declaration=true")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "questionsAndAnswers": [
            {
              "question": "WHERE_DID_THEY_LIVE",
              "answer": {
                "answer": "NO_PERMANENT_OR_FIXED",
                "@class": "StringAnswer"
              }
            }
          ]
        }
        """.trimIndent(),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), authSource = "nomis"))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-2.sql")
  fun `Skipping BCST2 assessment when it is not started - forbidden`() {
    val nomsId = "G4161UF"

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/skip?assessmentType=BCST2")
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .header("Content-Type", "application/json")
      .bodyValue(
        """
          {
            "reason": "COMPLETED_IN_OASYS",
            "moreInfo": "something or other"
          }
        """,
      )
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-resettlement-assessment-3.sql")
  fun `Post resettlement assessment validate - forbidden`() {
    val nomsId = "ABC1234"
    val pathway = "ACCOMMODATION"
    val assessmentType = "BCST2"
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/validate?assessmentType=$assessmentType")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "questionsAndAnswers": [
            {
              "question": "WHERE_DID_THEY_LIVE",
              "answer": {
                "answer": "NO_PERMANENT_OR_FIXED",
                "@class": "StringAnswer"
              }
            }
          ]
        }
        """.trimIndent(),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-2.sql")
  fun `Post get next assessment page - forbidden`() {
    val nomsId = "G1458GV"
    val pathway = "ACCOMMODATION"
    val assessmentType = "BCST2"
    val currentPage = "WHERE_DID_THEY_LIVE"
    val questions = listOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
      ResettlementAssessmentRequestQuestionAndAnswer(
        "WHERE_DID_THEY_LIVE",
        answer = StringAnswer("PRIVATE_RENTED_HOUSING"),
      ),
      ResettlementAssessmentRequestQuestionAndAnswer(
        "WHERE_DID_THEY_LIVE_ADDRESS",
        answer = MapAnswer(
          listOf(
            mapOf(
              "addressLine1" to "123 main street",
              "city" to "Leeds",
              "postcode" to "LS1 123",
            ),
          ),
        ),
      ),
    )
    val body = ResettlementAssessmentRequest(
      questionsAndAnswers = questions,
    )

    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/$pathway/next-page?assessmentType=$assessmentType&currentPage=$currentPage")
      .bodyValue(body)
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isForbidden
  }
}
