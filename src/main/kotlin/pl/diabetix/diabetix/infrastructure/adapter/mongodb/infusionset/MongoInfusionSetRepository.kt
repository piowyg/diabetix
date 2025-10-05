package pl.diabetix.diabetix.infrastructure.adapter.mongodb.infusionset

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import pl.diabetix.diabetix.domain.InfusionSet
import pl.diabetix.diabetix.domain.InfusionSetNotFoundException
import pl.diabetix.diabetix.domain.InfusionSetRepository
import pl.diabetix.diabetix.domain.UserId
import java.time.LocalDate

@Component
class MongoInfusionSetRepository(
    private val repository: PersistentInfusionSetRepository
) : InfusionSetRepository {

    override fun create(infusionSet: InfusionSet): InfusionSet =
        repository.save(infusionSet.asPersistentInfusionSet())
            .also { logger.info { "Creating new infusion set id ${it.id} userId ${it.userId}" } }
            .toDomain()

    override fun update(infusionSet: InfusionSet): InfusionSet {
        findById(infusionSet.id)

        return repository.save(infusionSet.asPersistentInfusionSet())
            .also { logger.info { "Updated infusion set with id ${it.id} userId ${it.userId}" } }
            .toDomain()
    }

    override fun findInfusionSetsByUserId(userId: UserId): List<InfusionSet> =
        repository.findByUserId(userId)
            .map { it.toDomain() }

    override fun findActiveInfusionSetByUserId(userId: UserId) =
        repository.findActiveByUserId(userId)
            ?.toDomain()

    override fun findById(id: String): InfusionSet =
        repository.findById(id)
            .orElseThrow { InfusionSetNotFoundException("Infusion set with id $id not found")}
            .toDomain()

    override fun findByRemovalDeadline(date: LocalDate): List<InfusionSet> =
        repository.findByRemovalDeadline(date).map { it.toDomain() }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
