package pl.diabetix.diabetix.adapter.mongodb.user

import org.springframework.data.mongodb.repository.MongoRepository

interface PersistentUserRepository : MongoRepository<PersistentUser, String>
