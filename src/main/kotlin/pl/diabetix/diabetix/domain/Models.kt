package pl.diabetix.diabetix.domain

import java.time.LocalDate

typealias UserId = String
typealias InfusionSetId = String

data class Authority(val name: String)

enum class BodyLocation {
    ARM,
    LEG,
    STOMACH,
    THIGH,
    BACK
}

data class User(
    val id: UserId,
    val email: String,
    val login: String,
    val name: String,
    val surname: String,
    val birthdate: LocalDate,
    val activated: Boolean
)

data class InfusionSetCommand(
    val bodyLocation: BodyLocation,
    val userId: String,
    val insertionDate: LocalDate
)

data class InfusionSetUpdateCommand(
    val bodyLocation: BodyLocation?,
    val removalDate: LocalDate?,
    val insertionDate: LocalDate?
)

data class InfusionSet(
    val id: InfusionSetId,
    val bodyLocation: BodyLocation,
    val userId: UserId,
    val isActive: Boolean,
    val insertionDate: LocalDate,
    val removalDeadline: LocalDate,
    val removalDate: LocalDate? = null
)
