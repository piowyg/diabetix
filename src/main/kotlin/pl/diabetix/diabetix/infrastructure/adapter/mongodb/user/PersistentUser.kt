package pl.diabetix.diabetix.infrastructure.adapter.mongodb.user

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import pl.diabetix.diabetix.domain.User
import java.time.Instant
import java.time.LocalDate

@Document(collection = "users")
data class PersistentUser (
    @Id val id: String,
    @Indexed(unique = true)
    val email: String,
    @Indexed(unique = true)
    val login: String,
    val name: String,
    val surname: String,
    val activated: Boolean,
    val birthdate: LocalDate,
    @CreatedDate
    val createdAt: Instant,
    @LastModifiedDate
    val updatedAt: Instant
)

internal fun PersistentUser.toDomain() = User(
    id = this.id,
    email = this.email,
    login = this.login,
    name = this.name,
    surname = this.surname,
    birthdate = this.birthdate,
    activated = this.activated
)
