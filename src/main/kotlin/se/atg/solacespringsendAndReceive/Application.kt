package se.atg.solacespringsendAndReceive

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SolaceSpringSendAndReceiveApplication

fun main(args: Array<String>) {
	runApplication<SolaceSpringSendAndReceiveApplication>(*args)
}
