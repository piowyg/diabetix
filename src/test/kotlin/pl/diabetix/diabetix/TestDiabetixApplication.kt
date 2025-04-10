package pl.diabetix.diabetix

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<DiabetixApplication>().with(TestcontainersConfiguration::class).run(*args)
}
