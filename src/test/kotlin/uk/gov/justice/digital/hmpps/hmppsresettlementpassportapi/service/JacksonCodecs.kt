package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonEncoder

val jacksonCodecsConfigurer: (t: ClientCodecConfigurer) -> Unit = {
  it.defaultCodecs()
    .jackson2JsonEncoder(
      Jackson2JsonEncoder(
        jacksonObjectMapper()
          .registerModule(JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS),
      ),
    )
}
