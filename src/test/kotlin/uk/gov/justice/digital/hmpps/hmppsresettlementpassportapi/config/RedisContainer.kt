package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

object RedisContainer {
  val instance: GenericContainer<Nothing>? by lazy { startRedisContainer() }

  private fun startRedisContainer(): GenericContainer<Nothing> {
    log.info("Creating a Postgres database")
    return GenericContainer<Nothing>("postgres").apply {
      dockerImageName = "redis:7.0"
      withExposedPorts(6379)
      setWaitStrategy(Wait.forListeningPort())
      withReuse(true)
      start()
    }
  }

  private val log = LoggerFactory.getLogger(this::class.java)
}
