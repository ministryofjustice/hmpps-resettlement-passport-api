package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config

import org.slf4j.LoggerFactory
import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

object LocalStackContainer {
  private val log = LoggerFactory.getLogger(this::class.java)
  val instance by lazy { startLocalstackIfNotRunning() }

  fun setLocalStackProperties(registry: DynamicPropertyRegistry) {
    registry.add("hmpps.sqs.localstackUrl") { instance.getEndpointOverride(LocalStackContainer.Service.SNS) }
    registry.add("hmpps.sqs.region") { instance.region }
    registry.add("hmpps.s3.localstackUrl") { instance.endpoint.toString() }
    registry.add("hmpps.s3.region") { instance.region }
  }

  private fun startLocalstackIfNotRunning(): LocalStackContainer {
    log.info("Creating a localstack instance")
    val logConsumer = Slf4jLogConsumer(log).withPrefix("localstack")
    return LocalStackContainer(
      DockerImageName.parse("localstack/localstack").withTag("3"),
    ).apply {
      withServices(LocalStackContainer.Service.SNS, LocalStackContainer.Service.SQS, LocalStackContainer.Service.S3)
      withEnv("DEFAULT_REGION", "eu-west-2")
      waitingFor(
        Wait.forLogMessage(".*Ready.*", 1),
      )
      start()
      followOutput(logConsumer)
    }
  }
}
