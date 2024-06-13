package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.data

import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat
import org.junit.jupiter.api.Test

class LicenceConditionsTest {
  @Test
  fun `when the same licence conditions are received should be equal`() {
    assertThat(aLicenceConditions() == aLicenceConditions()).isTrue()
  }

  @Test
  fun `when the different licence conditions are received should not be equal`() {
    assertThat(aLicenceConditions() == anotherLicenceConditions()).isFalse()
  }

  private fun aLicenceConditions() = LicenceConditions(
    licenceId = 1,
    status = "status",
    startDate = "2024-01-01",
    expiryDate = "2024-04-01",
    standardLicenceConditions = listOf(
      Conditions(
        id = 1,
        image = false,
        text = "The first condition",
        sequence = 1,
      ),
    ),
    otherLicenseConditions = listOf(
      Conditions(
        id = 2,
        image = true,
        text = "Another condition",
        sequence = 2,
      ),
    ),
  )

  private fun anotherLicenceConditions() = LicenceConditions(
    licenceId = 2,
    status = "status",
    startDate = "2024-01-01",
    expiryDate = "2024-04-01",
    standardLicenceConditions = listOf(
      Conditions(
        id = 3,
        image = false,
        text = "The third condition",
        sequence = 1,
      ),
    ),
    otherLicenseConditions = listOf(
      Conditions(
        id = 2,
        image = true,
        text = "Another condition",
        sequence = 2,
      ),
    ),
  )
}
