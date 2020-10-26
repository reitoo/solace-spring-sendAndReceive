package se.atg.solacespringsendAndReceive

import brave.jms.delegate
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.core.MessageCreator
import java.util.*
import java.util.Collections.synchronizedMap
import javax.jms.*
import javax.jms.Queue

/**
 * JmsTemplate optimized for solace request/reply by caching the temporary reply queue and consumer on session identity.
 */
class SolaceJmsTemplate(connectionFactory: ConnectionFactory) : JmsTemplate(connectionFactory) {
    /** Weak to prevent memory leak if sessions are not reused */
    private val cache: MutableMap<Session, QueueAndConsumer> = synchronizedMap(WeakHashMap())

    override fun doSendAndReceive(
        session: Session,
        destination: Destination,
        messageCreator: MessageCreator
    ): Message? {
        val correlationId = UUID.randomUUID().toString()
        val (replyQueue, consumer) = getQueueAndConsumer(session)

        session.createProducer(destination).use { producer ->
            doSend(producer, messageCreator.createMessage(session).apply {
                jmsCorrelationID = correlationId
                jmsReplyTo = replyQueue
            })
        }
        return generateSequence { receiveFromConsumer(consumer, receiveTimeout) }
            .firstOrNull { it.jmsCorrelationID == correlationId }
    }

    /** Uses session.delegate for compatibility with sleuth TracingSession */
    private fun getQueueAndConsumer(session: Session): QueueAndConsumer = cache.computeIfAbsent(session.delegate) {
        val queue = session.createTemporaryQueue()
        QueueAndConsumer(queue, session.createConsumer(queue))
    }

    private data class QueueAndConsumer(val queue: Queue, val consumer: MessageConsumer)
}