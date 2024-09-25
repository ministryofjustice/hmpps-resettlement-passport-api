package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql

class TodoIntegrationTest : IntegrationTestBase() {
  private val minimalTask = mapOf(
    "task" to "make some tea",
    "urn" to "urn123",
  )

  @Nested
  inner class CreateTodoItem {
    @Test
    @Sql("/testdata/sql/seed-pop-user-otp.sql")
    fun `create initial todo item minimal`() {
      authedWebTestClient.post()
        .uri("/resettlement-passport/person/G4161UF/todo")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(minimalTask)
        .exchange()
        .expectStatus()
        .isCreated()
    }

    @Test
    @Sql("/testdata/sql/seed-pop-user-otp.sql")
    fun `create initial todo item all fields`() {
      authedWebTestClient.post()
        .uri("/resettlement-passport/person/G4161UF/todo")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          mapOf(
            "task" to "make some coffee",
            "urn" to "urn456",
            "dueDate" to "2020-11-30",
            "notes" to "No milk",
          ),
        )
        .exchange()
        .expectStatus()
        .isCreated()
    }

    @Test
    fun `should 401 with no authentication header on create`() {
      webTestClient.post()
        .uri("/resettlement-passport/person/G4161UF/todo")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(minimalTask)
        .exchange()
        .expectStatus()
        .isUnauthorized()
    }

    @Test
    fun `should 403 with incorrect role header on create`() {
      webTestClient.post()
        .uri("/resettlement-passport/person/G4161UF/todo")
        .contentType(MediaType.APPLICATION_JSON)
        .headers(setAuthorisation(roles = listOf("SOME_ROLE_IDK")))
        .bodyValue(minimalTask)
        .exchange()
        .expectStatus()
        .isForbidden()
    }

    @Test
    fun `should 404 on create if no person found by nomis id`() {
      authedWebTestClient.post()
        .uri("/resettlement-passport/person/DUNNO/todo")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(minimalTask)
        .exchange()
        .expectStatus()
        .isNotFound()
    }
  }

  @Nested
  inner class GetTodoList {
    @Test
    @Sql("/testdata/sql/seed-pop-user-otp.sql")
    fun `create initial todo item`() {
      createTodoItem("task" to "do the first thing", "urn" to "urn1")
      createTodoItem(
        "task" to "do the second thing",
        "urn" to "urn1",
        "notes" to "note this",
        "dueDate" to "2020-11-30",
      )

      authedWebTestClient.get()
        .uri("/resettlement-passport/person/G4161UF/todo")
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .json(readFile("testdata/expectation/todo-with-2-items.json"))
    }

    @Test
    @Sql("/testdata/sql/seed-pop-user-otp.sql")
    fun `gives an empty array when person has no todo items`() {
      authedWebTestClient.get()
        .uri("/resettlement-passport/person/G4161UF/todo")
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader().contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .json("[]")
    }

    @Test
    fun `should 401 with no authentication header on get`() {
      webTestClient.get()
        .uri("/resettlement-passport/person/G4161UF/todo")
        .exchange()
        .expectStatus()
        .isUnauthorized()
    }

    @Test
    fun `should 403 with incorrect role header on get`() {
      webTestClient.get()
        .uri("/resettlement-passport/person/G4161UF/todo")
        .headers(setAuthorisation(roles = listOf("SOME_ROLE_IDK")))
        .exchange()
        .expectStatus()
        .isForbidden()
    }

    @Test
    fun `should 404 when no person found by nomis id on get`() {
      authedWebTestClient.get()
        .uri("/resettlement-passport/person/UNKNOWNST/todo")
        .exchange()
        .expectStatus()
        .isNotFound()
    }
  }

  fun createTodoItem(vararg data: Pair<String, String>) {
    authedWebTestClient.post()
      .uri("/resettlement-passport/person/G4161UF/todo")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(data.toMap())
      .exchange()
      .expectStatus()
      .isCreated()
  }
}
