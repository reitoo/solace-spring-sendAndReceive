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
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.MessageCreator
import org.springframework.jms.listener.DefaultMessageListenerContainer.CACHE_CONSUMER
import org.springframework.jms.support.JmsUtils
import org.springframework.stereotype.Component
import org.springframework.util.Assert
import java.util.concurrent.ConcurrentHashMap
import javax.jms.*

@Configuration
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
        }

    @Bean
    fun cachingConnectionFactory(connectionFactory: ConnectionFactory, config: SolaceProperties) =
        CachingConnectionFactory(connectionFactory).apply {
            sessionCacheSize = config.clientThreads
        }

    @Bean
    fun jmsTemplate(cachingConnectionFactory: ConnectionFactory) =
        CustomJmsTemplate(cachingConnectionFactory).apply {
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

@Component
class CustomJmsTemplate(connectionFactory: ConnectionFactory) : JmsTemplate(connectionFactory) {
    init {
        isExplicitQosEnabled = true
        receiveTimeout = 4000
        isPubSubDomain = false
    }

    val cache: MutableMap<Session, CachedStuff> = ConcurrentHashMap<Session, CachedStuff>()

    override fun doSendAndReceive(
        session: Session,
        destination: Destination,
        messageCreator: MessageCreator
    ): Message? {
        Assert.notNull(messageCreator, "MessageCreator must not be null")
        var producer: MessageProducer? = null
        return try {
            val requestMessage = messageCreator.createMessage(session)
            val (responseQueue, consumer) = cache.computeIfAbsent(session) {
                val queue = session.createTemporaryQueue()
                CachedStuff(queue, session.createConsumer(queue))
            }
            producer = session.createProducer(destination)
            requestMessage.jmsReplyTo = responseQueue
            if (logger.isDebugEnabled) {
                logger.debug("Sending created message: $requestMessage")
            }
            doSend(producer, requestMessage)
            receiveFromConsumer(consumer, receiveTimeout)
        } finally {
            JmsUtils.closeMessageProducer(producer)
        }
    }

    data class CachedStuff(val queue: Queue, val consumer: MessageConsumer)
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