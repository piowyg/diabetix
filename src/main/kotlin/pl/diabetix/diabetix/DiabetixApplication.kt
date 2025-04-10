package pl.diabetix.diabetix

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DiabetixApplication

fun main(args: Array<String>) {
    runApplication<DiabetixApplication>(*args)
}
