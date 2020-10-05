package se.atg.solacespringsendAndReceive

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.jms.core.JmsTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@RestController
@RequestMapping("/test")
class RestController (val jmsTemplate : JmsTemplate){

    @GetMapping("/{testId}")
    fun triggerTest(@PathVariable testId : String): String {
        val resultMessage = jmsTemplate.sendAndReceive(SOLACE_WORK_QUEUE) { session ->
             session.createTextMessage(testId)
        }?: throw RuntimeException("timeout waiting for message")
        val result = jmsTemplate.messageConverter!!.fromMessage(resultMessage)
        return result.toString()
    }

}