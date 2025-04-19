package pl.diabetix.diabetix.domain

import java.time.LocalDate

data class User(
    val id: UserId,
    val email: String,
    val login: String,
    val name: String,
    val surname: String,
    val birthdate: LocalDate
)

data class CreateUser(
    val email: String,
    val password: String,
    val login: String,
    val name: String,
    val surname: String,
    val birthdate: LocalDate
)

typealias UserId = String

