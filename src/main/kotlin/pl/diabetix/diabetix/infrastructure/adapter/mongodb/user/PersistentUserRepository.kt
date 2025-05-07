package pl.diabetix.diabetix.infrastructure.adapter.mongodb.user

import org.springframework.data.mongodb.repository.MongoRepository

interface PersistentUserRepository : MongoRepository<PersistentUser, String> {
    fun findByLogin(login: String): PersistentUser?
}
