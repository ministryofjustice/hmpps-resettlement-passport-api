package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import org.slf4j.LoggerFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait

object PostgresContainer {
  val instance: PostgreSQLContainer<Nothing>? by lazy { startPostgresqlContainer() }

  private fun startPostgresqlContainer(): PostgreSQLContainer<Nothing> {
    log.info("Creating a Postgres database")
    return PostgreSQLContainer<Nothing>("postgres:16").apply {
      withEnv("HOSTNAME_EXTERNAL", "localhost")
      withDatabaseName("resettlement-passport")
      withUsername("resettlement-passport")
      withPassword("resettlement-passport")
      setWaitStrategy(Wait.forListeningPort())
      withReuse(true)

      start()
    }
  }

  private val log = LoggerFactory.getLogger(this::class.java)
}
