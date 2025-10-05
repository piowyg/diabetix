package pl.diabetix.diabetix.application.scheduled

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import pl.diabetix.diabetix.application.notification.MessageSender
import pl.diabetix.diabetix.domain.DateTimeProvider
import pl.diabetix.diabetix.domain.InfusionSetRepository

@Component
class InfusionSetRemovalScheduler(
    private val infusionSetRepository: InfusionSetRepository,
    private val dateTimeProvider: DateTimeProvider,
    private val messageSenders: Set<MessageSender>
) {

    @Scheduled(cron = "\${scheduling.infusion-set-removal.cron}", zone = "Europe/Warsaw")
    fun checkTodayRemovals() {
        val today = dateTimeProvider.currentLocalDate()
        val toRemoveToday = infusionSetRepository.findByRemovalDeadline(today)
        if (toRemoveToday.isEmpty()) {
            logger.info { "No infusion sets with removalDate == $today" }
            return
        }

        logger.info { "Found ${toRemoveToday.size} infusion set(s) with removalDate == $today" }

        toRemoveToday.forEach { set ->
            val message = "InfusionSet id=${set.id}, userId=${set.userId} has removalDate == $today"
            messageSenders.forEach { sender ->
                try {
                    sender.send(message)
                } catch (ex: Exception) {
                    logger.error(ex) { "Failed to send message via ${sender.javaClass.simpleName} for infusionSet=${set.id}" }
                }
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
