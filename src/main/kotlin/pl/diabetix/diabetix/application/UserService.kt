package pl.diabetix.diabetix.application

import org.springframework.stereotype.Component
import pl.diabetix.diabetix.domain.CreateUser
import pl.diabetix.diabetix.domain.User
import pl.diabetix.diabetix.domain.UserRepository

@Component
class UserService(
    private val userRepository: UserRepository,
) {

    fun create(user: CreateUser): User = userRepository.create(user)
}
