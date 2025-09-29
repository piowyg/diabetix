package pl.diabetix.diabetix.infrastructure.adapter.mongodb.infusionset

import org.springframework.data.mongodb.repository.MongoRepository
import pl.diabetix.diabetix.domain.UserId

/**
 * Repository for managing PersistentInfusionSet entities in MongoDB.
 */
interface PersistentInfusionSetRepository: MongoRepository<PersistentInfusionSet, String> {
    /**
     * Returns all infusion sets for a given user.
     *
     * @param userId the user identifier
     * @return list of infusion sets for the user
     */
    fun findByUserId(userId: UserId): List<PersistentInfusionSet>

    /**
     * Returns a single active infusion set for a given user (i.e. the one that does not have a removal date set), or null if none exists.
     *
     * @param userId the user identifier
     * @return the active infusion set (removalDate == null) or null
     */
    @org.springframework.data.mongodb.repository.Query("{ 'userId': ?0, 'isActive': true }")
    fun findActiveByUserId(userId: UserId): PersistentInfusionSet?
}
