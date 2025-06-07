package pl.diabetix.diabetix.domain

import java.time.LocalDate

data class Authority(val name: String)

data class User(
    val id: UserId,
    val email: String,
    val login: String,
    val name: String,
    val surname: String,
    val birthdate: LocalDate,
    val activated: Boolean
)

typealias UserId = String
