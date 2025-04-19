package pl.diabetix.diabetix.adapter.mongodb.user

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pl.diabetix.diabetix.adapter.mongodb.IdGenerator.generateId
import pl.diabetix.diabetix.domain.CreateUser
import pl.diabetix.diabetix.domain.User
import java.time.LocalDate
import java.time.LocalDateTime

@Document(collection = "users")
data class PersistentUser (
    @Id val id: String,
    val email: String,
    val login: String,
    val name: String,
    val surname: String,
    val password: String,
    val birthdate: LocalDate,
    @CreatedDate
    val createdAt: LocalDateTime
)

internal fun CreateUser.asPersistentUser() = PersistentUser(
    email = this.email,
    password = this.password,
    login = this.login,
    name = this.name,
    surname = this.surname,
    birthdate = this.birthdate,
    id = generateId(),
    createdAt = LocalDateTime.now()
)

internal fun PersistentUser.toDomain() = User(
    id = this.id,
    email = this.email,
    login = this.login,
    name = this.name,
    surname = this.surname,
    birthdate = this.birthdate,
)