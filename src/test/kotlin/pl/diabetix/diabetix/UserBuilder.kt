package pl.diabetix.diabetix

import pl.diabetix.diabetix.domain.User
import java.time.LocalDate

class UserBuilder {
    private var id: String = "fake-id"
    private var login: String = "fake-login"
    private var email: String = "fake@email.com"
    private var name: String = "FakeName"
    private var surname: String = "FakeSurname"
    private var birthdate: LocalDate = LocalDate.of(2000, 1, 1)
    private var activated: Boolean = true

    fun id(id: String) = apply { this.id = id }
    fun login(login: String) = apply { this.login = login }
    fun email(email: String) = apply { this.email = email }
    fun name(name: String) = apply { this.name = name }
    fun surname(surname: String) = apply { this.surname = surname }
    fun birthdate(birthdate: LocalDate) = apply { this.birthdate = birthdate }
    fun activated(activated: Boolean) = apply { this.activated = activated }

    fun build(): User = User(
        id = id,
        login = login,
        email = email,
        name = name,
        surname = surname,
        birthdate = birthdate,
        activated = activated
    )
}

