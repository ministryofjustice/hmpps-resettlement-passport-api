package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.helpers

import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.LocalStackContainer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.LocalStackContainer.setLocalStackProperties
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.PostgresContainer
import uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config.RedisContainer

@ActiveProfiles("test")
abstract class TestBase {

  companion object {
    private val pgContainer = PostgresContainer.instance
    private val redisContainer = RedisContainer.instance
    private val localStackContainer = LocalStackContainer.instance

    @JvmStatic
    @DynamicPropertySource
    fun properties(registry: DynamicPropertyRegistry) {
      pgContainer?.run {
        registry.add("spring.flyway.url", pgContainer::getJdbcUrl)
        registry.add("spring.flyway.user", pgContainer::getUsername)
        registry.add("spring.flyway.password", pgContainer::getPassword)
        registry.add("spring.datasource.url", pgContainer::getJdbcUrl)
        registry.add("spring.datasource.user", pgContainer::getUsername)
        registry.add("spring.datasource.password", pgContainer::getPassword)
      }
      redisContainer?.run {
        registry.add("spring.data.redis.host", redisContainer::getHost)
        registry.add("spring.data.redis.port") { redisContainer.getMappedPort(6379).toString() }
        registry.add("spring.data.redis.ssl.enabled") { false }
      }

      localStackContainer.run {
        setLocalStackProperties(registry)
      }
    }
  }
}
