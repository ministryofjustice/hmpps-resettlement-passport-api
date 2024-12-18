package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayStatusAndCaseNote
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Category
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.ContactType
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.DeliusContactEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.DeliusContactRepository
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import java.time.LocalDateTime

class PathwayIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var pathwayStatusRepository: PathwayStatusRepository

  @Autowired
  private lateinit var deliusContactRepository: DeliusContactRepository

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-4.sql")
  fun `Patch pathway status and case notes happy path - NOMIS user`() {
    val nomsId = "G4274GN"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)
    caseNotesApiMockServer.stubPostCaseNotes(nomsId, "RESET", "ACCOM", "This is a case note", "MDI", 200)

    webTestClient.patch()
      .uri("/resettlement-passport/prisoner/$nomsId/pathway-with-case-note")
      .bodyValue(
        PathwayStatusAndCaseNote(
          pathway = Pathway.ACCOMMODATION,
          status = Status.IN_PROGRESS,
          caseNoteText = "This is a case note",
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), authSource = "nomis"))
      .exchange()
      .expectStatus().isOk

    val expectedPathwayStatus =
      PathwayStatusEntity(
        1,
        1,
        Pathway.ACCOMMODATION,
        Status.IN_PROGRESS,
        LocalDateTime.now(),
      )
    val actualPathwayStatus = pathwayStatusRepository.findById(1)

    assertThat(expectedPathwayStatus).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java)
      .isEqualTo(actualPathwayStatus.get())
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-4.sql")
  fun `Patch pathway status and case notes happy path - Delius user`() {
    val nomsId = "G4274GN"
    prisonerSearchApiMockServer.stubGetPrisonerDetails(nomsId, 200)

    webTestClient.patch()
      .uri("/resettlement-passport/prisoner/$nomsId/pathway-with-case-note")
      .bodyValue(
        PathwayStatusAndCaseNote(
          pathway = Pathway.ACCOMMODATION,
          status = Status.IN_PROGRESS,
          caseNoteText = "This is a case note",
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), authSource = "delius"))
      .exchange()
      .expectStatus().isOk

    val expectedPrisoner = PrisonerEntity(
      1,
      "G4274GN",
      LocalDateTime.parse("2023-08-16T12:21:38.709"),
      "MDI",
    )

    val expectedPathwayStatus =
      PathwayStatusEntity(
        1,
        1,
        Pathway.ACCOMMODATION,
        Status.IN_PROGRESS,
        LocalDateTime.now(),
      )
    val actualPathwayStatus = pathwayStatusRepository.findById(1)

    val expectedDeliusContact = DeliusContactEntity(
      id = 1,
      prisonerId = expectedPrisoner.id(),
      category = Category.ACCOMMODATION,
      contactType = ContactType.CASE_NOTE,
      createdDate = LocalDateTime.now(),
      notes = "This is a case note",
      createdBy = "RESETTLEMENTPASSPORT_ADM",
    )

    val actualDeliusContact = deliusContactRepository.findById(1)

    assertThat(actualPathwayStatus.get()).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java)
      .isEqualTo(expectedPathwayStatus)
    assertThat(actualDeliusContact.get()).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java)
      .isEqualTo(expectedDeliusContact)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Patch pathway status and case notes happy path - 404 on prisoner`() {
    val nomsId = "abc"

    webTestClient.patch()
      .uri("/resettlement-passport/prisoner/$nomsId/pathway-with-case-note")
      .bodyValue(
        PathwayStatusAndCaseNote(
          pathway = Pathway.ACCOMMODATION,
          status = Status.IN_PROGRESS,
          caseNoteText = "Case note text",
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), authSource = "nomis"))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage")
      .isEqualTo("Resource not found. Check request parameters - Prisoner with id abc not found in database")
      .jsonPath("developerMessage").isEqualTo("Prisoner with id abc not found in database")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses-1.sql")
  fun `Patch pathway status and case notes happy path - 404 on pathway status`() {
    val nomsId = "789"

    webTestClient.patch()
      .uri("/resettlement-passport/prisoner/$nomsId/pathway-with-case-note")
      .bodyValue(
        PathwayStatusAndCaseNote(
          pathway = Pathway.ACCOMMODATION,
          status = Status.IN_PROGRESS,
          caseNoteText = "Case note text",
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), authSource = "nomis"))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage")
      .isEqualTo("Resource not found. Check request parameters - Prisoner with id 789 has no pathway_status entry for ACCOMMODATION in database")
      .jsonPath("developerMessage")
      .isEqualTo("Prisoner with id 789 has no pathway_status entry for ACCOMMODATION in database")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  fun `Patch pathway status and case notes happy path - 401`() {
    val nomsId = "123"

    webTestClient.patch()
      .uri("/resettlement-passport/prisoner/$nomsId/pathway-with-case-note")
      .bodyValue(
        PathwayStatusAndCaseNote(
          pathway = Pathway.ACCOMMODATION,
          status = Status.IN_PROGRESS,
          caseNoteText = "Case note text",
        ),
      )
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Patch pathway status and case notes happy path - forbidden`() {
    val nomsId = "123"

    webTestClient.patch()
      .uri("/resettlement-passport/prisoner/$nomsId/pathway-with-case-note")
      .headers(setAuthorisation())
      .bodyValue(
        PathwayStatusAndCaseNote(
          pathway = Pathway.ACCOMMODATION,
          status = Status.IN_PROGRESS,
          caseNoteText = "Case note text",
        ),
      )
      .exchange()
      .expectStatus().isForbidden
  }

  @Test
  fun `Patch pathway status and case notes happy path - 400`() {
    val nomsId = "123"

    webTestClient.patch()
      .uri("/resettlement-passport/prisoner/$nomsId/pathway-with-case-note")
      .header("Content-Type", "application/json")
      .bodyValue(
        """
          {
            "pathway": "FAKE_PATHWAY",
            "status": "IN_PROGRESS",
            "caseNoteText": "Case note text"
          }
        """.trimIndent(),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_RESETTLEMENT_PASSPORT_EDIT"), authSource = "nomis"))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(400)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage").isEqualTo("Validation failure - please check request parameters and try again")
      .jsonPath("developerMessage").value { message: String ->
        assertThat(message).contains(
          """pathway: must be one of [ACCOMMODATION, ATTITUDES_THINKING_AND_BEHAVIOUR, CHILDREN_FAMILIES_AND_COMMUNITY, DRUGS_AND_ALCOHOL, EDUCATION_SKILLS_AND_WORK, FINANCE_AND_ID, HEALTH]""",
        )
      }
      .jsonPath("moreInfo").isEmpty
  }
}
