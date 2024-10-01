package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.JsonPathAssertions
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

class TodoIntegrationTest : IntegrationTestBase() {
  private val minimalTask = mapOf(
    "title" to "make some tea",
    "urn" to "urn123",
  )

  @Nested
  @DisplayName("create")
  inner class CreateTodoItem {
    @Test
    @Sql("/testdata/sql/seed-1-prisoner.sql")
    fun `create initial todo item minimal`() {
      authedWebTestClient.post()
        .uri("/resettlement-passport/person/CRN123/todo")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(minimalTask)
        .exchange()
        .expectStatus()
        .isCreated()
        .expectBody()
        .jsonPath("$.id").isUuid()
    }

    @Test
    @Sql("/testdata/sql/seed-1-prisoner.sql")
    fun `create initial todo item all fields`() {
      authedWebTestClient.post()
        .uri("/resettlement-passport/person/CRN123/todo")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(
          mapOf(
            "title" to "make some coffee",
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
        .uri("/resettlement-passport/person/CRN123/todo")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(minimalTask)
        .exchange()
        .expectStatus()
        .isUnauthorized()
    }

    @Test
    fun `should 403 with incorrect role header on create`() {
      webTestClient.post()
        .uri("/resettlement-passport/person/CRN123/todo")
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
  @DisplayName("get all")
  inner class GetTodoList {
    @Test
    @Sql("/testdata/sql/seed-1-prisoner.sql")
    fun `view list with 2 items`() {
      createTodoItem("title" to "do the first thing", "urn" to "urn1")
      createTodoItem(
        "title" to "do the second thing",
        "urn" to "urn1",
        "notes" to "note this",
        "dueDate" to "2020-11-30",
      )

      getTodoItems()
        .expectBody()
        .json(readFile("testdata/expectation/todo-with-2-items.json"))
    }

    @Test
    @Sql("/testdata/sql/seed-1-prisoner.sql")
    fun `view list with 3 items sorted by date`() {
      createTodoItem("title" to "early", "urn" to "urn1", "dueDate" to "2024-10-01")
      createTodoItem("title" to "later", "urn" to "urn1", "dueDate" to "2024-11-30")
      createTodoItem("title" to "middle", "urn" to "urn1", "dueDate" to "2024-10-15")

      getTodoItems("sortField=dueDate&sortDirection=DESC")
        .expectBody()
        .jsonPath("$.[0].title").isEqualTo("later")
        .jsonPath("$.[1].title").isEqualTo("middle")
        .jsonPath("$.[2].title").isEqualTo("early")
    }

    @Test
    @Sql("/testdata/sql/seed-1-prisoner.sql")
    fun `gives an empty array when person has no todo items`() {
      getTodoItems()
        .expectBody()
        .json("[]")
    }

    @Test
    fun `should 401 with no authentication header on get`() {
      webTestClient.get()
        .uri("/resettlement-passport/person/CRN123/todo")
        .exchange()
        .expectStatus()
        .isUnauthorized()
    }

    @Test
    fun `should 403 with incorrect role header on get`() {
      webTestClient.get()
        .uri("/resettlement-passport/person/CRN123/todo")
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

    @Test
    fun `should 400 when an invalid sort field is given`() {
      authedWebTestClient.get()
        .uri("/resettlement-passport/person/123/todo?sortField=potato")
        .exchange()
        .expectStatus()
        .isBadRequest()
    }

    @Test
    fun `should 400 when an invalid sort direction is given`() {
      authedWebTestClient.get()
        .uri("/resettlement-passport/person/123/todo?sortDirection=SIDEWAYS")
        .exchange()
        .expectStatus()
        .isBadRequest()
    }
  }

  private fun getTodoItems(params: String = ""): WebTestClient.ResponseSpec {
    val query = if (params.isNotEmpty()) "?$params" else ""
    return authedWebTestClient.get()
      .uri("/resettlement-passport/person/CRN123/todo$query")
      .exchange()
      .expectStatus()
      .isOk()
      .expectHeader().contentType(MediaType.APPLICATION_JSON)
  }

  @Nested
  @DisplayName("delete")
  inner class DeleteTodoItem {
    @Test
    @Sql("/testdata/sql/seed-1-prisoner.sql")
    fun `can delete an item from todo list`() {
      val id = createTodoItem("title" to "make some toast", "urn" to "urn5")["id"]

      authedWebTestClient.delete()
        .uri("/resettlement-passport/person/CRN123/todo/$id")
        .exchange()
        .expectStatus()
        .isNoContent()

      getTodoItems().expectBody().json("[]")
    }

    @Test
    @Sql("/testdata/sql/seed-1-prisoner.sql")
    fun `should 404 when todo item not found for delete`() {
      authedWebTestClient.delete()
        .uri("/resettlement-passport/person/CRN123/todo/${UUID.randomUUID()}")
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
        .uri("/resettlement-passport/person/UNKNOWN/todo/${UUID.randomUUID()}")
        .exchange()
        .expectStatus()
        .isUnauthorized()
    }

    @Test
    fun `should 403 with incorrect role header on delete`() {
      webTestClient.delete()
        .uri("/resettlement-passport/person/CRN123/todo/${UUID.randomUUID()}")
        .headers(setAuthorisation(roles = listOf("SOME_ROLE_IDK")))
        .exchange()
        .expectStatus()
        .isForbidden()
    }
  }

  @Nested
  @DisplayName("patch")
  inner class PatchTodoItem {

    @Test
    @Sql("/testdata/sql/seed-1-prisoner.sql")
    fun `should be able complete an item`() {
      val id = createTodoItem("title" to "make some toast", "urn" to "urn5")["id"]
      authedWebTestClient.patch()
        .uri("/resettlement-passport/person/CRN123/todo/$id")
        .bodyValue(
          mapOf(
            "urn" to "urn6",
            "completed" to true,
          ),
        )
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json(readFile("testdata/expectation/todo-patch-response.json"))
    }

    @Test
    @Sql("/testdata/sql/seed-1-prisoner.sql")
    fun `should 404 when todo item not found for patch`() {
      authedWebTestClient.patch()
        .uri("/resettlement-passport/person/CRN123/todo/${UUID.randomUUID()}")
        .bodyValue(minimalTask)
        .exchange()
        .expectStatus()
        .isNotFound()
    }

    @Test
    fun `should 404 when person item not found for patch`() {
      authedWebTestClient.put()
        .uri("/resettlement-passport/person/UNKNOWN/todo/${UUID.randomUUID()}")
        .bodyValue(minimalTask)
        .exchange()
        .expectStatus()
        .isNotFound()
    }

    @Test
    fun `should 401 with no authentication header on patch`() {
      webTestClient.put()
        .uri("/resettlement-passport/person/UNKNOWN/todo/${UUID.randomUUID()}")
        .bodyValue(minimalTask)
        .exchange()
        .expectStatus()
        .isUnauthorized()
    }

    @Test
    fun `should 403 with incorrect role header on patch`() {
      webTestClient.put()
        .uri("/resettlement-passport/person/CRN123/todo/${UUID.randomUUID()}")
        .bodyValue(minimalTask)
        .headers(setAuthorisation(roles = listOf("SOME_ROLE_IDK")))
        .exchange()
        .expectStatus()
        .isForbidden()
    }
  }

  @Nested
  @DisplayName("update")
  inner class UpdateTodoItem {

    @Test
    @Sql("/testdata/sql/seed-1-prisoner.sql")
    fun `should update todo item`() {
      val id = createTodoItem("title" to "make some toast", "urn" to "urn5")["id"]
      authedWebTestClient.put()
        .uri("/resettlement-passport/person/CRN123/todo/$id")
        .bodyValue(
          mapOf(
            "urn" to "urn6",
            "title" to "slice some bread",
            "notes" to "do not toast",
            "dueDate" to "2024-09-28",
          ),
        )
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json(readFile("testdata/expectation/todo-update-response.json"))
    }

    @Test
    @Sql("/testdata/sql/seed-1-prisoner.sql")
    fun `should 404 when todo item not found for update`() {
      authedWebTestClient.put()
        .uri("/resettlement-passport/person/CRN123/todo/${UUID.randomUUID()}")
        .bodyValue(minimalTask)
        .exchange()
        .expectStatus()
        .isNotFound()
    }

    @Test
    fun `should 404 when person item not found for update`() {
      authedWebTestClient.put()
        .uri("/resettlement-passport/person/UNKNOWN/todo/${UUID.randomUUID()}")
        .bodyValue(minimalTask)
        .exchange()
        .expectStatus()
        .isNotFound()
    }

    @Test
    fun `should 401 with no authentication header on update`() {
      webTestClient.put()
        .uri("/resettlement-passport/person/UNKNOWN/todo/${UUID.randomUUID()}")
        .bodyValue(minimalTask)
        .exchange()
        .expectStatus()
        .isUnauthorized()
    }

    @Test
    fun `should 403 with incorrect role header on update`() {
      webTestClient.put()
        .uri("/resettlement-passport/person/CRN123/todo/${UUID.randomUUID()}")
        .bodyValue(minimalTask)
        .headers(setAuthorisation(roles = listOf("SOME_ROLE_IDK")))
        .exchange()
        .expectStatus()
        .isForbidden()
    }
  }

  @Nested
  @DisplayName("get one")
  inner class GetTodoItem {

    @Test
    @Sql("/testdata/sql/seed-1-prisoner.sql")
    fun `should get single todo item`() {
      val id = createTodoItem(
        "title" to "make some toast",
        "urn" to "urn5",
        "notes" to "white bread",
        "dueDate" to "2024-09-30",
      )["id"]

      authedWebTestClient.get()
        .uri("/resettlement-passport/person/CRN123/todo/$id")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .json(readFile("testdata/expectation/todo-get-response.json"))
    }

    @Test
    @Sql("/testdata/sql/seed-1-prisoner.sql")
    fun `should 404 when todo item not found for get`() {
      authedWebTestClient.get()
        .uri("/resettlement-passport/person/CRN123/todo/${UUID.randomUUID()}")
        .exchange()
        .expectStatus()
        .isNotFound()
    }

    @Test
    fun `should 404 when person item not found for get`() {
      authedWebTestClient.get()
        .uri("/resettlement-passport/person/UNKNOWN/todo/${UUID.randomUUID()}")
        .exchange()
        .expectStatus()
        .isNotFound()
    }

    @Test
    fun `should 401 with no authentication header on update`() {
      webTestClient.get()
        .uri("/resettlement-passport/person/UNKNOWN/todo/${UUID.randomUUID()}")
        .exchange()
        .expectStatus()
        .isUnauthorized()
    }

    @Test
    fun `should 403 with incorrect role header on get`() {
      webTestClient.get()
        .uri("/resettlement-passport/person/CRN123/todo/${UUID.randomUUID()}")
        .headers(setAuthorisation(roles = listOf("SOME_ROLE_IDK")))
        .exchange()
        .expectStatus()
        .isForbidden()
    }
  }

  fun createTodoItem(vararg data: Pair<String, String>): Map<*, *> = authedWebTestClient.post()
    .uri("/resettlement-passport/person/CRN123/todo")
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
