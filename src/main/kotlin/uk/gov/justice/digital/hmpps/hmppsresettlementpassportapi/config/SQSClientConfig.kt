package uk.gov.justice.digital.hmpps.hmppsresettlementpassportapi.config;

import com.amazon.sqs.javamessaging.ProviderConfiguration
import com.amazon.sqs.javamessaging.SQSConnectionFactory
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import jakarta.jms.ConnectionFactory
import jakarta.jms.Session
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.annotation.EnableJms
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.destination.DynamicDestinationResolver
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.services.sqs.SqsClient


@Configuration
@EnableJms


class JmsConfig(private val connectionFactory: ConnectionFactory) {
  private fun sqsConnectionFactory(): ConnectionFactory {
  val sqsClient = SqsClient.builder()
    .region(Region.getRegion(Regions.EU_WEST_2))
    .credentialsProvider(DefaultCredentialsProvider.create())
    .build()
  return SQSConnectionFactory(ProviderConfiguration(), sqsClient)
}

    @Bean
    fun jmsListenerContainerFactory(): DefaultJmsListenerContainerFactory {
        val factory = DefaultJmsListenerContainerFactory()
        factory.setConnectionFactory(sqsConnectionFactory())
        factory.setDestinationResolver(DynamicDestinationResolver())
        factory.setConcurrency("3-10")
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE)
        return factory
    }

    @Bean
    fun defaultJmsTemplate(): JmsTemplate {
        return JmsTemplate(connectionFactory)
    }
}


