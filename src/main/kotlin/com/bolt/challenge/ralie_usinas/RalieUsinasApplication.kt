package com.bolt.challenge.ralie_usinas

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class RalieUsinasApplication

fun main(args: Array<String>) {
	runApplication<RalieUsinasApplication>(*args)
}
