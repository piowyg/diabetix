package pl.diabetix.diabetix.application

import org.springframework.stereotype.Component
import pl.diabetix.diabetix.domain.User
import java.time.LocalDate

@Component
class UserDataFactory {

    fun getUser(details: Map<String, Any>): User {
        var activated = true
        val sub: String = details.get("sub").toString()
        val id: String
        var login = details["preferred_username"]?.toString()?.lowercase()

        // handle resource server JWT, where sub claim is email and uid is ID
        if (details["uid"] != null) {
            id = details["uid"].toString()
            login = sub
        } else {
            id = sub
        }

        if (login == null) {
            login = id
        }

        val firstName = if (details["given_name"] != null) {
            details["given_name"].toString()
        } else if (details["name"] != null) {
            details["name"].toString()
        } else {
            throw IllegalStateException("")
        }

        val surname = details["family_name"]?.toString() ?: throw IllegalStateException("")
        val birthDate = details["birthdate"]?.toString()?.let { LocalDate.parse(it) } ?: throw IllegalStateException("")

        val email = if (details["email"] != null) {
            details["email"].toString().lowercase()
        } else if (sub.contains("|") && (login.contains("@"))) {
            // special handling for Auth0
            login
        } else {
            sub
        }

        details["email_verified"]?.apply {
            activated = details["email_verified"] as Boolean
        }

        return User(
            id = id,
            login = login,
            email = email,
            name = firstName,
            surname = surname,
            activated = activated,
            birthdate = birthDate
        )
    }
}
