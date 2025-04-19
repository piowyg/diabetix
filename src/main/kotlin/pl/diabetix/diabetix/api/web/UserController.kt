package pl.diabetix.diabetix.api.web

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import pl.diabetix.diabetix.application.UserDataFactory
import pl.diabetix.diabetix.application.UserService
import pl.diabetix.diabetix.domain.User
import java.time.LocalDate

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val userDataFactory: UserDataFactory
) {

    // TODO:
    //  check how return 201 status
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    fun createUser(@Valid @RequestBody request: CreateUserRequest) =
        userDataFactory.convertToCreateUser(request)
            .let { userService.create(it).toResponse() }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateUserRequest(
    @Email
    val email: String,
    @NotBlank(message = "Password cannot be blank")
    val password: String,
    @NotNull
    val login: String,
    @NotNull
    val name: String,
    @NotNull
    val surname: String,
    @Past(message = "Birthdate must be in the past")
    val birthdate: LocalDate
)

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
