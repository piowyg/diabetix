package pl.diabetix.diabetix.application.notification

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.StandardOpenOption

/**
 * Implementation of Local MessageSender that adds an event with a notification to the macOS Calendar.
 * Requires macOS (osascript) and the Calendar app.
 */
@Component
class LocalCalendarMessageSender(
    @Value("\${notification.calendar.name:Home}")
    private val calendarName: String
) : MessageSender {

    override fun send(message: String) {
        if (!isMacOs()) {
            logger.warn { "CalendarMessageSender skipped: not macOS" }
            return
        }

        val script = buildAppleScript(message)
        runAppleScript(script)
    }

    private fun buildAppleScript(message: String): String {
        val safeMessage = message.replace("\"", "\\\"")
        val title = "Infusion set removal"
        return """
            tell application "Calendar"
                set calName to "$calendarName"
                set targetCal to calendar 1
                try
                    set targetCal to calendar calName
                end try
                set startDate to (current date)
                set hours of startDate to 21
                set minutes of startDate to 0
                set seconds of startDate to 0
                set endDate to startDate + 900 -- +15 minutes
                set newEvent to make new event at end of events of targetCal with properties {summary:"$title", description:"$safeMessage", start date:startDate, end date:endDate}
                tell newEvent
                    make new display alarm at end with properties {trigger interval:0}
                end tell
            end tell
        """.trimIndent()
    }

    private fun runAppleScript(script: String) {
        var tmpFile: java.nio.file.Path? = null
        try {
            tmpFile = Files.createTempFile("diabetix_calendar_", ".applescript")
            Files.writeString(tmpFile, script, StandardOpenOption.TRUNCATE_EXISTING)

            val pb = ProcessBuilder("osascript", tmpFile.toAbsolutePath().toString())
            val process = pb.redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            if (exitCode != 0) {
                logger.error { "osascript failed (exit=$exitCode). Output: $output" }
            } else if (output.isNotBlank()) {
                logger.info { "osascript output: $output" }
            }
        } catch (ex: Exception) {
            logger.error(ex) { "Failed to execute AppleScript for Calendar event" }
        } finally {
            try {
                tmpFile?.let { Files.deleteIfExists(it) }
            } catch (_: Exception) {}
        }
    }

    private fun isMacOs(): Boolean =
        System.getProperty("os.name")?.lowercase()?.contains("mac") == true

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
