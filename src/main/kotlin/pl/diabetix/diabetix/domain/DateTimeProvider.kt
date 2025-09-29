package pl.diabetix.diabetix.domain

import java.time.Instant
import java.time.LocalDate

interface DateTimeProvider {
    fun currentInstant(): Instant
    fun currentLocalDate(): LocalDate
}

class DefaultDateTimeProvider() : DateTimeProvider {

    override fun currentInstant(): Instant {
        return Instant.now()
    }

    override fun currentLocalDate(): LocalDate {
        return LocalDate.now()
    }
}