package pl.diabetix.diabetix

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import pl.diabetix.diabetix.infrastructure.ApplicationProperties

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties::class)
class DiabetixApplication

fun main(args: Array<String>) {
    runApplication<DiabetixApplication>(*args)
}
