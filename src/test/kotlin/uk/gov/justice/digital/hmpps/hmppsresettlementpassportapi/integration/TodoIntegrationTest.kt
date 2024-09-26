package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.JsonPathAssertions
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

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
        .expectBody()
        .jsonPath("$.id").isUuid()
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
    fun `view list with 2 items`() {
      createTodoItem("task" to "do the first thing", "urn" to "urn1")
      createTodoItem(
        "task" to "do the second thing",
        "urn" to "urn1",
        "notes" to "note this",
        "dueDate" to "2020-11-30",
      )

      getTodoItems()
        .expectBody()
        .json(readFile("testdata/expectation/todo-with-2-items.json"))
    }

    @Test
    @Sql("/testdata/sql/seed-pop-user-otp.sql")
    fun `gives an empty array when person has no todo items`() {
      getTodoItems()
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

  private fun getTodoItems(): WebTestClient.ResponseSpec =
    authedWebTestClient.get()
      .uri("/resettlement-passport/person/G4161UF/todo")
      .exchange()
      .expectStatus()
      .isOk()
      .expectHeader().contentType(MediaType.APPLICATION_JSON)

  @Nested
  inner class DeleteTodoItem {
    @Test
    @Sql("/testdata/sql/seed-pop-user-otp.sql")
    fun `can delete an item from todo list`() {
      val id = createTodoItem("task" to "make some toast", "urn" to "urn5")["id"]

      authedWebTestClient.delete()
        .uri("/resettlement-passport/person/G4161UF/todo/$id")
        .exchange()
        .expectStatus()
        .isNoContent()

      getTodoItems().expectBody().json("[]")
    }

    @Test
    @Sql("/testdata/sql/seed-pop-user-otp.sql")
    fun `should 404 when todo item not found for delete`() {
      authedWebTestClient.delete()
        .uri("/resettlement-passport/person/G4161UF/todo/${UUID.randomUUID()}")
        .exchange()
        .expectStatus()
        .isNotFound()
    }

    @Test
    fun `should 404 when person item not found for delete`() {
      authedWebTestClient.delete()
        .uri("/resettlement-passport/person/UNKNOWN/todo/${UUID.randomUUID()}")
        .exchange()
        .expectStatus()
        .isNotFound()
    }

    @Test
    fun `should 401 with no authentication header on delete`() {
      webTestClient.delete()
        .exchange()
        .expectStatus()
        .isUnauthorized()
    }

    @Test
    fun `should 403 with incorrect role header on get`() {
      webTestClient.delete()
        .uri("/resettlement-passport/person/G4161UF/todo/${UUID.randomUUID()}")
        .headers(setAuthorisation(roles = listOf("SOME_ROLE_IDK")))
        .exchange()
        .expectStatus()
        .isForbidden()
    }
  }

  fun createTodoItem(vararg data: Pair<String, String>): Map<*, *> = authedWebTestClient.post()
    .uri("/resettlement-passport/person/G4161UF/todo")
    .contentType(MediaType.APPLICATION_JSON)
    .bodyValue(data.toMap())
    .exchange()
    .expectStatus()
    .isCreated()
    .expectHeader().contentType(MediaType.APPLICATION_JSON)
    .expectBody(Map::class.java)
    .returnResult().responseBody!!
}

private fun JsonPathAssertions.isUuid() {
  this.value { v: Any ->
    try {
      UUID.fromString(v as String)
    } catch (e: Exception) {
      Assertions.fail("$v is not a UUID")
    }
  }
}
