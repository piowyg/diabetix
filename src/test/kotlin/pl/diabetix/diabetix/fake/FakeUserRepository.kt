package pl.diabetix.diabetix.fake

import pl.diabetix.diabetix.domain.User
import pl.diabetix.diabetix.domain.UserRepository

class FakeUserRepository : UserRepository {
    private val users = mutableMapOf<String, User>()
    override fun findUserByLogin(login: String): User? = users[login]
    override fun create(user: User): User {
        users[user.login] = user
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