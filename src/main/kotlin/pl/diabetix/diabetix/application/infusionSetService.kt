package pl.diabetix.diabetix.application

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import pl.diabetix.diabetix.domain.DateTimeProvider
import pl.diabetix.diabetix.domain.InfusionSet
import pl.diabetix.diabetix.domain.InfusionSetCommand
import pl.diabetix.diabetix.domain.InfusionSetRepository
import pl.diabetix.diabetix.domain.UserId
import pl.diabetix.diabetix.domain.UserNotFoundException

/**
 * Service responsible for managing infusion sets for users.
 * Handles creation, retrieval, and removal of infusion sets, ensuring business rules such as only one active infusion set per user.
 */
@Component
class InfusionSetService(
    private val infusionSetRepository: InfusionSetRepository,
    private val infusionSetFactory: InfusionSetDataFactory,
    private val userService: UserService,
    private val dateTimeProvider: DateTimeProvider
) {
    /**
     * Creates a new infusion set for a user. If the user does not exist, throws UserNotFoundException.
     * If there is an active infusion set for the user, it will be removed (removalDate set and isActive=false).
     *
     * @param infusionSet the infusion set to create
     * @return the created infusion set
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional
    fun create(infusionSetCommand: InfusionSetCommand): InfusionSet {
        if (!userService.existsById(infusionSetCommand.userId)) {
            throw UserNotFoundException("User with id ${infusionSetCommand.userId} does not exist.")
        }

        // check if there is an existing infusion set for the user
        removeActiveInfusionSet(infusionSetCommand.userId)

        return infusionSetRepository.create(infusionSetFactory.create(infusionSetCommand))
    }

    /**
     * Returns the active infusion set for a given user, or null if none exists.
     *
     * @param userId the user identifier
     * @return the active infusion set or null
     */
    fun getActiveInfusionSetByUserId(userId: UserId): InfusionSet? =
        infusionSetRepository.findActiveInfusionSetByUserId(userId)

    /**
     * Returns all infusion sets for a given user.
     *
     *  @param userId the user identifier
     *  @return list of infusion sets for the user
     */
    fun getInfusionSetsByUserId(userId: UserId): List<InfusionSet> =
        infusionSetRepository.findInfusionSetsByUserId(userId)

    /**
     * Removes the active infusion set for a user by setting its removalDate to the current date and marking it as inactive.
     *
     * @param userId the user identifier
     */
    private fun removeActiveInfusionSet(userId: UserId) {
        val activeInfusionSet = getActiveInfusionSetByUserId(userId)
        activeInfusionSet?.let {
            infusionSetRepository.update(it.copy(removalDate = dateTimeProvider.currentLocalDate(), isActive = false))
                .also { logger.info { "Automated remove infusion set" } }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

