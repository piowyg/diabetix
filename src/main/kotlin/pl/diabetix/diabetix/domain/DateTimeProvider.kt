package pl.diabetix.diabetix.domain

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

interface DateTimeProvider {
    fun currentDateTime(): OffsetDateTime
    fun currentInstant(): Instant
}

class DefaultDateTimeProvider(timeZoneId: String) : DateTimeProvider {
    private val timeZone = ZoneId.of(timeZoneId)

    override fun currentDateTime(): OffsetDateTime {
        return OffsetDateTime.now()
    }

    override fun currentInstant(): Instant {
        return Instant.now()
    }
}