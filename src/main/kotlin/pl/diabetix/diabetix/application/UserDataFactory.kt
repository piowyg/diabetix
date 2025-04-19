package pl.diabetix.diabetix.application

import org.springframework.stereotype.Component
import pl.diabetix.diabetix.api.web.CreateUserRequest
import pl.diabetix.diabetix.domain.CreateUser

@Component
class UserDataFactory {

    fun convertToCreateUser(user: CreateUserRequest) = CreateUser(
        email = user.email,
        password = user.password,
        login = user.login,
        name = user.name,
        surname = user.surname,
        birthdate = user.birthdate
    )
}
