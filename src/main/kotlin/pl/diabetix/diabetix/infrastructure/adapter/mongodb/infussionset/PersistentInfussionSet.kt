package pl.diabetix.diabetix.infrastructure.adapter.mongodb.infussionset

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import pl.diabetix.diabetix.domain.BodyLocation
import pl.diabetix.diabetix.domain.InfusionSet
import java.time.Instant
import java.time.LocalDate

@Document(collection = "infusion_sets")
data class PersistentInfusionSet(
    @Id val id: String,
    val bodyLocation: BodyLocation,
    val userId: String,
    val insertionDate: LocalDate,
    val removalDeadline: LocalDate,
    val removalDate: LocalDate? = null,
    val isActive: Boolean = true,
    @CreatedDate
    val createdAt: Instant?,
    @LastModifiedDate
    val updatedAt: Instant?
)

internal fun PersistentInfusionSet.toDomain() = InfusionSet(
    id = this.id,
    bodyLocation = this.bodyLocation,
    userId = this.userId,
    insertionDate = this.insertionDate,
    removalDeadline = this.removalDeadline,
    removalDate = this.removalDate,
    isActive = this.isActive
)

internal fun InfusionSet.asPersistentInfusionSet() = PersistentInfusionSet(
    id = this.id,
    bodyLocation = this.bodyLocation,
    userId = this.userId,
    insertionDate = this.insertionDate,
    removalDeadline = this.removalDeadline,
    removalDate = this.removalDate,
    isActive = this.isActive,
    createdAt = null,
    updatedAt = null
)
