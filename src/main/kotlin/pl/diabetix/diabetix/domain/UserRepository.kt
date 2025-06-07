package pl.diabetix.diabetix.domain

interface UserRepository {
    fun create(user: User): User
    fun update(user: User): User
    fun findUserByLogin(login: String): User?
}
