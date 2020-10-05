package se.atg.solacespringsendAndReceive

import com.solacesystems.jms.SolJmsUtility
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.springframework.jms.connection.CachingConnectionFactory
import org.springframework.jms.core.JmsTemplate
import javax.jms.ConnectionFactory

@Configuration
@Profile("!activemq")
@EnableConfigurationProperties(SolaceProperties::class)
class SolaceConfig {

    @Bean
    fun connectionFactory(config: SolaceProperties) =
        SolJmsUtility.createConnectionFactory().apply {
            host = config.host
            vpn = config.vpn
            username = config.username
            password = config.password
            directTransport = false
            dmqEligible = true
            connectTimeoutInMillis = config.connectTimeoutInMillis
            connectRetries = config.connectRetries
            connectRetriesPerHost = config.connectRetriesPerHost
            reconnectRetryWaitInMillis = config.reconnectRetryWaitInMillis
        }

    @Bean
    fun cachingConnectionFactory(
        connectionFactory: ConnectionFactory,
        config: SolaceProperties
    ) = CachingConnectionFactory(connectionFactory)
        .apply { sessionCacheSize = config.concurrency }

    @Bean
    fun jmsTemplate(cachingConnectionFactory: ConnectionFactory) =
        JmsTemplate(cachingConnectionFactory)
            .apply {
                isExplicitQosEnabled = true
                setReceiveTimeout(4000)
                isPubSubDomain = false
            }

    @Bean
    fun listenerContainerFactory(connectionFactory: ConnectionFactory) = DefaultJmsListenerContainerFactory().apply {
        setConnectionFactory(connectionFactory)
        setSessionTransacted(true)
        setPubSubDomain(false)
        setTaskExecutor(SimpleAsyncTaskExecutor("JmsSolace-"))
    }
}


@ConstructorBinding
@ConfigurationProperties("solace")
data class SolaceProperties(
    val host: String,
    val vpn: String,
    val username: String,
    val password: String,
    @Value("\${solace.concurrency.events}")
    val concurrency: Int = 10,
    val connectTimeoutInMillis: Int = 200,
    val connectRetries: Int = -1,
    val reconnectRetries: Int = -1,
    val connectRetriesPerHost: Int = 0,
    val reconnectRetryWaitInMillis: Int = 200
)