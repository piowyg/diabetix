package pl.diabetix.diabetix.infrastructure

import pl.diabetix.diabetix.domain.DateTimeProvider
import java.time.Instant
import java.time.OffsetDateTime

class FixedTestDateTimeProvider : DateTimeProvider {

   var currentTime: OffsetDateTime = OffsetDateTime.now()

    fun setDateTime(time: OffsetDateTime) {
        currentTime = time
    }

    override fun currentDateTime(): OffsetDateTime {
        return currentTime
    }

    override fun currentInstant(): Instant {
        return currentTime.toInstant()
    }
}