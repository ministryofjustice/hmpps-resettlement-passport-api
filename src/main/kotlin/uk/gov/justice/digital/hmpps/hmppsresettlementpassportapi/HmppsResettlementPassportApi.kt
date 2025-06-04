package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.FeatureFlagProperties

/**
 * Used as the system oauth username when calling HMPPS apis
 * as well as the "audit module" for creating records in prison-api
 */
const val SYSTEM_USERNAME = "RESETTLEMENT_PASSPORT_API"

@SpringBootApplication()
@EnableConfigurationProperties(FeatureFlagProperties::class)
class HmppsResettlementPassportApi

fun main(args: Array<String>) {
  runApplication<HmppsResettlementPassportApi>(*args)
}
