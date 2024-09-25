package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql

class TodoIntegrationTest : IntegrationTestBase() {
  @Test
  @Sql("/testdata/sql/seed-pop-user-otp.sql")
  fun `create initial list`() {
    authedWebTestClient.post()
      .uri("/resettlement-passport/person/G4161UF/todo")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        """
        {
          "task": "make some tea",
          "urn": "urn123"
        }
        """.trimIndent(),
      )
      .exchange()
      .expectStatus()
      .isCreated()
  }
}
