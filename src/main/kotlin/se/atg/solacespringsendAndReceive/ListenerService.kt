package se.atg.solacespringsendAndReceive

import org.slf4j.LoggerFactory
import org.springframework.jms.annotation.JmsListener
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.util.ErrorHandler

@Service
class ListenerService {
    @JmsListener(destination = "\${solace.work-queue}", concurrency = "\${solace.listener-threads}")
    fun onMessage() = "pong"
}

@Component
class ListenerErrorHandler : ErrorHandler {
    val logger = LoggerFactory.getLogger(WorkGenerator::class.java)!!
    override fun handleError(throwable: Throwable) = logger.warn("Ignoring {}", throwable.toString())
}