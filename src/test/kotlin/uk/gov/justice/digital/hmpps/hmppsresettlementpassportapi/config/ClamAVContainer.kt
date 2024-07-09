package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import io.jsonwebtoken.io.IOException
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.net.BindException
import java.net.ServerSocket

object ClamAVContainer {
  private val log = LoggerFactory.getLogger(this::class.java)
  val instance by lazy { startClamAVIfNotRunning() }

  private fun startClamAVIfNotRunning(): GenericContainer<*>? {
    if (clamAVIsRunning()) return null
    val logConsumer = Slf4jLogConsumer(log).withPrefix("clamAV")
    return GenericContainer(DockerImageName.parse("ghcr.io/ministryofjustice/hmpps-clamav-freshclammed:latest"))
      .withExposedPorts(3310)
      .withAccessToHost(true)
      .apply {
        waitingFor(
          Wait.forLogMessage(".*Set stacksize to.*", 1),
        )
        start()
        followOutput(logConsumer)
      }
  }

  private fun clamAVIsRunning(): Boolean = try {
    val serverSocket = ServerSocket(3310)
    serverSocket.localPort == 0
  } catch (e: IOException) {
    true
  } catch (e: BindException) {
    true
  }
}
