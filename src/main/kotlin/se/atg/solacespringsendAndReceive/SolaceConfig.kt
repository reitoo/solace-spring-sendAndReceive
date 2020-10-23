package se.atg.solacespringsendAndReceive

import org.apache.activemq.broker.BrokerPlugin
import org.apache.activemq.broker.BrokerService
import org.apache.activemq.broker.util.DestinationPathSeparatorBroker
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.listener.DefaultMessageListenerContainer.CACHE_CONSUMER
import javax.jms.ConnectionFactory

@Configuration
@EnableConfigurationProperties(SolaceProperties::class)
class SolaceConfig {

    @Bean
    fun broker(): BrokerService {
        val broker = BrokerService()
        broker.isPersistent = false
        broker.plugins = arrayOf<BrokerPlugin>(DestinationPathSeparatorBroker())
        broker.addConnector("tcp://localhost:61616")
        broker.isUseJmx = false
        return broker
    }

    @Bean
    fun jmsTemplate(connectionFactory: ConnectionFactory) =
        JmsTemplate(connectionFactory).apply {
            isExplicitQosEnabled = true
            receiveTimeout = 4000
            isPubSubDomain = false
        }

    @Bean
    fun jmsListenerContainerFactory(connectionFactory: ConnectionFactory, errorHandler: ListenerErrorHandler) =
        DefaultJmsListenerContainerFactory().apply {
            setConnectionFactory(connectionFactory)
            setPubSubDomain(false)
            setTaskExecutor(SimpleAsyncTaskExecutor("listener-"))
            setErrorHandler(errorHandler)
            setCacheLevel(CACHE_CONSUMER)
        }
}


@ConstructorBinding
@ConfigurationProperties("solace")
data class SolaceProperties(
    val host: String,
    val vpn: String,
    val username: String,
    val password: String,
    val clientThreads: Int = 10,
    val listenerThreads: Int = 10,
    val workQueue: String
)