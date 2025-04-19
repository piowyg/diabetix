package pl.diabetix.diabetix.domain

data class CreateUserException(override val message: String) : RuntimeException(message)