package se.atg.solacespringsendAndReceive

import org.springframework.jms.annotation.JmsListener
import org.springframework.messaging.Message
import org.springframework.stereotype.Service

const val SOLACE_WORK_QUEUE = "core-betting-service.work"

@Service
class ListenerService {

    @JmsListener(
        destination = SOLACE_WORK_QUEUE,
        containerFactory = "listenerContainerFactory",
        concurrency = "\${workqueue.listeners:4}"
    )
    fun onMessage(message: Message<String>): String {
        return "replay to test: ${message.payload}"
    }

}