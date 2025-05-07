package pl.diabetix.diabetix.api.web

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.web.bind.annotation.*
import pl.diabetix.diabetix.application.UserService
import pl.diabetix.diabetix.domain.User
import pl.diabetix.diabetix.domain.UserNotFoundException
import java.security.Principal
import java.time.LocalDate

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {
    @GetMapping("/account")
    fun getAccount(principal: Principal): UserResponse? {
        if (principal is AbstractAuthenticationToken) {
            return userService.getUserFromAuthentication(principal).toResponse()
        } else {
            throw UserNotFoundException("User could not be found")
        }
    }
}

fun User.toResponse() = UserResponse(
    id = this.id,
    email = this.email,
    name = this.name,
    surname = this.surname,
    birthdate = this.birthdate,
    login = this.login,
)

data class UserResponse(
    val id: String,
    val email: String,
    val login: String,
    val name: String,
    val surname: String,
    val birthdate: LocalDate
)
