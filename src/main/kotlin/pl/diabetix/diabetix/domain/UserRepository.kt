package pl.diabetix.diabetix.domain

interface UserRepository {
    fun create(user: CreateUser): User
    fun update(user: User): User
    fun findUserById(id: String): User?
    fun findUserByLogin(login: String): User?
}