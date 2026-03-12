package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.springframework.test.web.reactive.server.WebTestClient
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule
import tools.jackson.module.kotlin.readValue

val jsonMapper = jsonMapper { addModule(kotlinModule()) }

inline fun <reified T> WebTestClient.ResponseSpec.returnBody(): T {
  val bodyJson: String = this
    .expectHeader().contentType("application/json")
    .expectBody(String::class.java)
    .returnResult()
    .responseBody!!

  return jsonMapper.readValue<T>(bodyJson)
}

inline fun <reified T> WebTestClient.ResponseSpec.expectBody(expected: T) {
  val actual = this.returnBody<T>()
  assertThat(actual).isEqualTo(expected)
}

inline fun <reified T> WebTestClient.ResponseSpec.assertBody(assertion: (T) -> Unit) {
  val actual = this.returnBody<T>()
  assertion(actual)
}
