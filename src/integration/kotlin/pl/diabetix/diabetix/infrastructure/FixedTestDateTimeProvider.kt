package pl.diabetix.diabetix.infrastructure

import pl.diabetix.diabetix.domain.DateTimeProvider
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class FixedTestDateTimeProvider : DateTimeProvider {

   var currentTime: Instant = Instant.now()

    fun setDateTime(time: Instant) {
        currentTime = time
    }

    override fun currentInstant(): Instant {
        return currentTime
    }

    override fun currentLocalDate(): LocalDate {
        return currentTime.atZone(ZoneId.of("UTC")).toLocalDate()
    }
}