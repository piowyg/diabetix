package pl.diabetix.diabetix.adapter.mongodb.user

import com.mongodb.DuplicateKeyException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import pl.diabetix.diabetix.domain.CreateUser
import pl.diabetix.diabetix.domain.CreateUserException
import pl.diabetix.diabetix.domain.User
import pl.diabetix.diabetix.domain.UserRepository

@Component
class MongoUserRepository(
    private val persistentUserRepository: PersistentUserRepository
) : UserRepository {

    override fun create(user: CreateUser): User {
        return try {
            user.asPersistentUser().let {
                logger.info { "Creating new user ${it.id}" }
                persistentUserRepository.save(it)
            }.toDomain()
        } catch (ex: DuplicateKeyException) {
            logger.error(ex) { "Trying to insert a user with already existing login: ${user.login} and email ${user.email}" }
            throw CreateUserException("User ${user.login} is already in use")
        }
    }

    override fun update(user: User): User {
        TODO("Not yet implemented")
    }

    override fun findUserById(id: String): User? {
        TODO("Not yet implemented")
    }

    override fun findUserByLogin(login: String): User? {
        TODO("Not yet implemented")
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}