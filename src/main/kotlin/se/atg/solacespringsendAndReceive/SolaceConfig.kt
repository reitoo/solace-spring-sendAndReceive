package se.atg.solacespringsendAndReceive

import com.solacesystems.jms.SolJmsUtility
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.springframework.jms.connection.CachingConnectionFactory
import org.springframework.jms.listener.DefaultMessageListenerContainer.CACHE_CONSUMER
import javax.jms.*

@Configuration
@EnableConfigurationProperties(SolaceProperties::class)
class SolaceConfig {

    @Bean
    fun connectionFactory(config: SolaceProperties): ConnectionFactory =
        SolJmsUtility.createConnectionFactory().apply {
            host = config.host
            vpn = config.vpn
            username = config.username
            password = config.password
            directTransport = false
            dmqEligible = true
        }

    @Bean
    fun cachingConnectionFactory(connectionFactory: ConnectionFactory, config: SolaceProperties) =
        CachingConnectionFactory(connectionFactory).apply {
            sessionCacheSize = config.clientThreads
        }

    @Bean
    fun jmsTemplate(cachingConnectionFactory: ConnectionFactory) =
        SolaceJmsTemplate(cachingConnectionFactory).apply {
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