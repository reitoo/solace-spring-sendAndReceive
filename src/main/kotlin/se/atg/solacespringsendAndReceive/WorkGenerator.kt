package se.atg.solacespringsendAndReceive

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.thread

@Component
class WorkGenerator(val jmsTemplate: JmsTemplate, val solaceProperties: SolaceProperties) {

    @EventListener
    fun run(e: ApplicationStartedEvent) {
        val logger = LoggerFactory.getLogger(WorkGenerator::class.java)!!
        val requestCounter = AtomicLong()

        // Create threads sending requests and waiting for replies.
        for (num in 1..solaceProperties.clientThreads) {
            thread(start = true, name = "client-$num") {
                while(true) {
                    jmsTemplate.sendAndReceive(solaceProperties.workQueue) { session -> session.createTextMessage("ping") }
                    requestCounter.incrementAndGet();
                }
            }
        }

        fixedRateTimer(period = 1000) {
            logger.info("Processed {} request/response calls.", requestCounter.getAndSet(0))
        }
    }
}