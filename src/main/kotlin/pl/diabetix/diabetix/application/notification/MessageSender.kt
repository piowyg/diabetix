package pl.diabetix.diabetix.application.notification

/**
 * Interface responsible for sending notification messages.
 * Implementations may send e.g. email, SMS, push notifications.
 */
interface MessageSender {
    fun send(message: String)
}
