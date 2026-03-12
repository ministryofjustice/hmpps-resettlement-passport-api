package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.integration

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.assertj.core.api.Assertions.assertThat
import org.springframework.test.web.reactive.server.WebTestClient

val jsonMapper: JsonMapper = jacksonMapperBuilder()
  .addModule(JavaTimeModule())
  .build()

inline fun <reified T> WebTestClient.ResponseSpec.returnBody(): T {
  val bodyJson: String = this
    .expectHeader().contentType("application/json")
    .expectBody(String::class.java)
    .returnResult()
    .responseBody!!

  return jsonMapper.readValue<T>(bodyJson, jacksonTypeRef<T>())
}

inline fun <reified T> WebTestClient.ResponseSpec.expectBody(expected: T) {
  val actual = this.returnBody<T>()
  assertThat(actual).isEqualTo(expected)
}

inline fun <reified T> WebTestClient.ResponseSpec.assertBody(assertion: (T) -> Unit) {
  val actual = this.returnBody<T>()
  assertion(actual)
}
