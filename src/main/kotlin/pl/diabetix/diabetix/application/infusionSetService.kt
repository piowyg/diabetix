package pl.diabetix.diabetix.application

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import pl.diabetix.diabetix.domain.*

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
     * Returns the infusion set with the given ID.
     *
     * @param id the ID of the infusion set
     * @return the infusion set with the given ID
     * @throws EntityNotFoundException if no infusion set with the given ID exists
     */
    fun getById(id: String): InfusionSet = infusionSetRepository.findById(id)

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
     **/
    @Transactional
    fun update(id: String, command: InfusionSetUpdateCommand): InfusionSet {
        val current = infusionSetRepository.findById(id)
        validateInfusionSet(command, current)

        val updatedInsertionDate = command.insertionDate ?: current.insertionDate
        val updatedRemovalDate = command.removalDate ?: current.removalDate

        val updated = current.copy(
            bodyLocation = command.bodyLocation ?: current.bodyLocation,
            removalDate = updatedRemovalDate,
            insertionDate = updatedInsertionDate,
            removalDeadline = updatedInsertionDate.plusDays(3), // Recalculate deadline based on new insertion date
            isActive = if (updatedRemovalDate != null) false else current.isActive
        )
        return infusionSetRepository.update(updated)
    }

    private fun validateInfusionSet(command: InfusionSetUpdateCommand, current: InfusionSet) {
        // Validate removal date if provided
        command.removalDate?.let {
            if (it < current.insertionDate) {
                logger.error { "Removal date $it cannot be before insertion date ${current.insertionDate}" }
                throw InvalidInfusionSetUpdateException("Removal date $it cannot be before insertion date ${current.insertionDate}")
            }
        }

        // Validate insertion date if provided
        command.insertionDate?.let { newInsertionDate ->
            // If removal date exists (either current or new), check that insertion date is not after removal date
            val finalRemovalDate = command.removalDate ?: current.removalDate
            finalRemovalDate?.let { removalDate ->
                if (newInsertionDate.isAfter(removalDate)) {
                    logger.error { "Insertion date $newInsertionDate cannot be after removal date $removalDate" }
                    throw InvalidInfusionSetUpdateException("Insertion date $newInsertionDate cannot be after removal date $removalDate")
                }
            }

            // insertion date should not be in the future
            val currentDate = dateTimeProvider.currentLocalDate()
            if (newInsertionDate.isAfter(currentDate)) {
                logger.error { "Insertion date $newInsertionDate cannot be in the future" }
                throw InvalidInfusionSetUpdateException("Insertion date $newInsertionDate cannot be in the future")
            }
        }
    }

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
