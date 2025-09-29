package pl.diabetix.diabetix.fake

import pl.diabetix.diabetix.domain.User
import pl.diabetix.diabetix.domain.UserId
import pl.diabetix.diabetix.domain.UserRepository

class FakeUserRepository : UserRepository {
    private val users = mutableMapOf<UserId, User>()
    override fun findUserById(id: UserId): User? = users[id]
    override fun existsById(id: UserId): Boolean = users[id] != null

    override fun create(user: User): User {
        users[user.id] = user
        return user
    }

    override fun update(user: User): User {
        TODO("Not yet implemented")
    }

    fun given(user: User) {
        users[user.login] = user
    }
    // Dodaj inne wymagane metody z UserRepository, jeśli są potrzebne
}