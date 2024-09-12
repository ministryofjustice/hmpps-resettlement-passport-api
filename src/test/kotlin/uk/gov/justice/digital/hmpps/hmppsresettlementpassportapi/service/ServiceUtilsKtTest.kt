package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.service

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test

class ServiceUtilsKtTest {
  @Test
  fun `creates a random string for otp`() {
    val randomAlphaNumericString = randomAlphaNumericString()
    assertThat(randomAlphaNumericString).matches("[0-9a-zA-Z]{6}")
  }
}
