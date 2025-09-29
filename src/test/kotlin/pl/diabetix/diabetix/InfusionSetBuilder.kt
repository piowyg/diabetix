package pl.diabetix.diabetix

import pl.diabetix.diabetix.domain.BodyLocation
import pl.diabetix.diabetix.domain.InfusionSet
import java.time.LocalDate

class InfusionSetBuilder {
    private var id: String = "123"
    private var bodyLocation: BodyLocation = BodyLocation.ARM
    private var userId: String = "1234"
    private var insertionDate: LocalDate = LocalDate.of(2025, 6, 8)
    private var removalDeadline: LocalDate = LocalDate.of(2025, 6, 11)
    private var removalDate: LocalDate? = null
    private var isActive: Boolean = true

    fun id(id: String) = apply { this.id = id }
    fun bodyLocation(bodyLocation: BodyLocation) = apply { this.bodyLocation = bodyLocation }
    fun userId(userId: String) = apply { this.userId = userId }
    fun insertionDate(insertionDate: LocalDate) = apply { this.insertionDate = insertionDate }
    fun removalDeadline(removalDeadline: LocalDate) = apply { this.removalDeadline = removalDeadline }
    fun removalDate(removalDate: LocalDate?) = apply { this.removalDate = removalDate }
    fun isActive(isActive: Boolean) = apply { this.isActive = isActive }

    fun build(): InfusionSet = InfusionSet(
        id = id,
        bodyLocation = bodyLocation,
        userId = userId,
        insertionDate = insertionDate,
        removalDeadline = removalDeadline,
        removalDate = removalDate,
        isActive = isActive,
    )
}
