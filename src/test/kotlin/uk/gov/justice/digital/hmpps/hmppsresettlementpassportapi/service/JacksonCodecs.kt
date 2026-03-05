package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.http.codec.json.JacksonJsonEncoder
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.module.kotlin.jacksonMapperBuilder

val jacksonCodecsConfigurer: (t: ClientCodecConfigurer) -> Unit = {
  it.defaultCodecs()
    .jacksonJsonEncoder(
      JacksonJsonEncoder(
        jacksonMapperBuilder()
          .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false)
          .configure(DateTimeFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
          .build(),
      ),
    )
}
