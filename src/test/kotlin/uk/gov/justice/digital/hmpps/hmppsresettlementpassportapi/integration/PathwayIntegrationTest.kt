package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data.PathwayAndStatus
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PathwayStatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.PrisonerEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Status
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.StatusEntity
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.repository.PathwayStatusRepository
import java.time.LocalDateTime

class PathwayIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var pathwayStatusRepository: PathwayStatusRepository

  private val fakeNow = LocalDateTime.parse("2023-08-17T12:00:01")

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses.sql")
  fun `Patch pathway status happy path`() {
    // Mock calls to LocalDateTime.now() so we can test the creationDate is being updated
    mockkStatic(LocalDateTime::class)
    every { LocalDateTime.now() } returns fakeNow

    val prisonerId = "123"

    webTestClient.patch()
      .uri("/resettlement-passport/prisoner/$prisonerId/pathway")
      .bodyValue(
        PathwayAndStatus(
          pathway = Pathway.ACCOMMODATION,
          status = Status.IN_PROGRESS,
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isOk

    val expectedPathwayStatus =
      PathwayStatusEntity(
        1,
        PrisonerEntity(
          1,
          "123",
          LocalDateTime.parse("2023-08-16T12:21:38.709"),
        ),
        PathwayEntity(
          1,
          "Accommodation",
          true,
          LocalDateTime.parse("2023-08-15T11:32:22.171"),
        ),
        StatusEntity(
          2,
          "In Progress",
          true,
          LocalDateTime.parse("2023-08-16T17:48:02.211790"),
        ),
        fakeNow,
      )
    val actualPathwayStatus = pathwayStatusRepository.findById(1).get()

    assertThat(expectedPathwayStatus).usingRecursiveComparison().ignoringFieldsOfTypes(LocalDateTime::class.java)
      .isEqualTo(actualPathwayStatus)
    Assertions.assertEquals(fakeNow, actualPathwayStatus.creationDate)

    unmockkStatic(LocalDateTime::class)
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses.sql")
  fun `Patch pathway status happy path - 404 on prisoner`() {
    val prisonerId = "abc"

    webTestClient.patch()
      .uri("/resettlement-passport/prisoner/$prisonerId/pathway")
      .bodyValue(
        PathwayAndStatus(
          pathway = Pathway.ACCOMMODATION,
          status = Status.IN_PROGRESS,
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage").isEqualTo("Resource not found. Check request parameters - Prisoner with id abc not found in database")
      .jsonPath("developerMessage").isEqualTo("Prisoner with id abc not found in database")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  @Sql("classpath:testdata/sql/seed-pathway-statuses.sql")
  fun `Patch pathway status happy path - 404 on pathway status`() {
    val prisonerId = "789"

    webTestClient.patch()
      .uri("/resettlement-passport/prisoner/$prisonerId/pathway")
      .bodyValue(
        PathwayAndStatus(
          pathway = Pathway.ACCOMMODATION,
          status = Status.IN_PROGRESS,
        ),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isNotFound
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(404)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage").isEqualTo("Resource not found. Check request parameters - Prisoner with id 789 has no pathway_status entry for Accommodation in database")
      .jsonPath("developerMessage").isEqualTo("Prisoner with id 789 has no pathway_status entry for Accommodation in database")
      .jsonPath("moreInfo").isEmpty
  }

  @Test
  fun `Patch pathway status happy path - 401`() {
    val prisonerId = "123"

    webTestClient.patch()
      .uri("/resettlement-passport/prisoner/$prisonerId/pathway")
      .bodyValue(
        PathwayAndStatus(
          pathway = Pathway.ACCOMMODATION,
          status = Status.IN_PROGRESS,
        ),
      )
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `Patch pathway status happy path - 400`() {
    val prisonerId = "123"

    webTestClient.patch()
      .uri("/resettlement-passport/prisoner/$prisonerId/pathway")
      .header("Content-Type", "application/json")
      .bodyValue(
        """
          {
            "pathway": "FAKE_PATHWAY",
            "status": "IN_PROGRESS"
          }
        """.trimIndent(),
      )
      .headers(setAuthorisation(roles = listOf("ROLE_ADMIN")))
      .exchange()
      .expectStatus().isBadRequest
      .expectHeader().contentType("application/json")
      .expectBody()
      .jsonPath("status").isEqualTo(400)
      .jsonPath("errorCode").isEmpty
      .jsonPath("userMessage").isEqualTo("Validation failure - please check request parameters and try again")
      .jsonPath("developerMessage").isEqualTo("JSON decoding error: Cannot deserialize value of type `uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.jpa.entity.Pathway` from String \"FAKE_PATHWAY\": not one of the values accepted for Enum class: [ACCOMMODATION, CHILDREN_FAMILIES_AND_COMMUNITY, FINANCE_AND_ID, DRUGS_AND_ALCOHOL, ATTITUDES_THINKING_AND_BEHAVIOUR, EDUCATION_SKILLS_AND_WORK, HEALTH]")
      .jsonPath("moreInfo").isEmpty
  }
}
