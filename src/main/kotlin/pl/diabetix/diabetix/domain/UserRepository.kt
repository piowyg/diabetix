package pl.diabetix.diabetix.domain

interface UserRepository {
    fun create(user: User): User
    fun update(user: User): User
    fun findUserById(id: UserId): User?
    fun existsById(id: UserId): Boolean
}
