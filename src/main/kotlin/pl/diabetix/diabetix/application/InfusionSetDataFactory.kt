package pl.diabetix.diabetix.application

import org.springframework.stereotype.Component
import pl.diabetix.diabetix.domain.InfusionSet
import pl.diabetix.diabetix.domain.InfusionSetCommand
import java.util.UUID

@Component
class InfusionSetDataFactory {

    fun create(command: InfusionSetCommand) = InfusionSet(
        id = generateId(),
        bodyLocation = command.bodyLocation,
        userId = command.userId,
        isActive = true,
        insertionDate = command.insertionDate,
        removalDeadline = command.insertionDate.plusDays(3),
        removalDate = null
    )

    private fun generateId(): String {
        return UUID.randomUUID().toString()
    }
}
