package pl.diabetix.diabetix.domain

data class CreateUserException(override val message: String) : RuntimeException(message)

data class UserNotFoundException(override val message: String) : RuntimeException(message)

data class InfusionSetNotFoundException(override val message: String) : RuntimeException(message)
