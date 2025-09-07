package pl.diabetix.diabetix.infrastructure.adapter.mongodb.user

import com.mongodb.DuplicateKeyException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import pl.diabetix.diabetix.domain.CreateUserException
import pl.diabetix.diabetix.domain.DateTimeProvider
import pl.diabetix.diabetix.domain.User
import pl.diabetix.diabetix.domain.UserId
import pl.diabetix.diabetix.domain.UserNotFoundException
import pl.diabetix.diabetix.domain.UserRepository
import java.time.Instant
import kotlin.jvm.optionals.getOrNull

@Component
class MongoUserRepository(
    private val persistentUserRepository: PersistentUserRepository,
    private val dateTimeProvider: DateTimeProvider
) : UserRepository {

    override fun create(user: User) = try {
        persistentUserRepository.save(user.asPersistentUser())
            .also { logger.info { "Creating new user ${it.id}" } }
            .toDomain()
    } catch (ex: DuplicateKeyException) {
        logger.error(ex) { "Trying to insert a user with already existing login: ${user.login} and email ${user.email}" }
        throw CreateUserException("User ${user.login} is already in use")
    }


    override fun update(user: User): User {
        val persistedUser = persistentUserRepository.findById(user.id)
            .orElseThrow { UserNotFoundException("User ${user.id} not found") }

        return persistentUserRepository.save(user.asPersistentUser(createdAt = persistedUser.createdAt))
            .also { logger.info { "Updating user ${it.id}" } }
            .toDomain()
    }

    override fun findUserById(id: UserId): User? = persistentUserRepository.findById(id).getOrNull()?.toDomain()

    override fun existsById(id: UserId): Boolean = persistentUserRepository.existsById(id)


    private fun User.asPersistentUser(createdAt: Instant? = null) = PersistentUser(
        id = this.id,
        email = this.email,
        login = this.login,
        name = this.name,
        surname = this.surname,
        birthdate = this.birthdate,
        activated = this.activated,
        createdAt = createdAt ?: dateTimeProvider.currentInstant(),
        updatedAt = dateTimeProvider.currentInstant()
    )

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
