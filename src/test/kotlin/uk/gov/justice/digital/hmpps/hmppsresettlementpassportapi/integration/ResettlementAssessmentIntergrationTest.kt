package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ListAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.MapAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequest
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.ResettlementAssessmentRequestQuestionAndAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.StringAnswer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ResettlementAssessmentType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.ResettlementAssessmentRepository

class ResettlementAssessmentIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var resettlementAssessmentRepository: ResettlementAssessmentRepository
  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-2.sql")
  fun `Get Next assessment page happy path`() {
    val expectedOutput = readFile("testdata/expectation/resettlement-assessment-1.json")
    val expectedOutput2 = readFile("testdata/expectation/resettlement-assessment-2.json")
    val nomsId = "G1458GV"
    var questions = mutableListOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
      ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE", answer = StringAnswer("NEW_ADDRESS"))
    )
    val body = ResettlementAssessmentRequest(
      pathway = Pathway.ACCOMMODATION,
      nomsID = nomsId,
      type = ResettlementAssessmentType.BCST2,
//      currentPage = AccommodationAssessmentPage.WHERE_WILL_THEY_LIVE,
      currentPage = "WHERE_WILL_THEY_LIVE",
      questions = questions,
      newStatus = Status.NOT_STARTED
    )
//    offenderSearchApiMockServer.stubGetPrisonersList(prisonId, "", 500, 0, 200)
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/next-page")
      .bodyValue(body)
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput)

    val test = resettlementAssessmentRepository.findAll()

    var questions2 = mutableListOf<ResettlementAssessmentRequestQuestionAndAnswer<*>>(
      ResettlementAssessmentRequestQuestionAndAnswer("WHERE_WILL_THEY_LIVE", answer = StringAnswer("NEW_ADDRESS")),
      ResettlementAssessmentRequestQuestionAndAnswer("WHO_WILL_THEY_LIVE_WITH",
        answer = MapAnswer(listOf(mapOf("name" to "person1", "age" to "47"), mapOf("name" to "person2", "age" to "53"))))
    )
    val body2 = ResettlementAssessmentRequest(
      pathway = Pathway.ACCOMMODATION,
      nomsID = nomsId,
      type = ResettlementAssessmentType.BCST2,
      currentPage = "WHO_WILL_THEY_LIVE_WITH",
      questions = questions2,
      newStatus = Status.NOT_STARTED
    )
    webTestClient.post()
      .uri("/resettlement-passport/prisoner/$nomsId/resettlement-assessment/next-page")
      .bodyValue(body2)
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT")))
      .exchange()
      .expectStatus().isOk
      .expectHeader().contentType("application/json")
      .expectBody().json(expectedOutput2)
    val test2 = resettlementAssessmentRepository.findAll()
    1+1
  }
}