package pl.diabetix.diabetix.api.web

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import pl.diabetix.diabetix.BaseIntegrationTest
import pl.diabetix.diabetix.api.web.ability.UserEndpointAbility
import pl.diabetix.diabetix.infrastructure.adapter.mongodb.user.PersistentUserRepository
import assertk.assertThat
import assertk.assertions.*

@DisplayName("UserController /users/account")
class UserControllerSpec : BaseIntegrationTest() {

    @Autowired
    lateinit var userAbility: UserEndpointAbility

    @Autowired
    lateinit var persistentUserRepository: PersistentUserRepository

    @Test
    fun `should return user account for authenticated user`() {
        // given, when
        val result = userAbility.callUserInfo()

        // then
        val userBody = result.expectStatus().isOk
            .expectBody(UserResponse::class.java).returnResult().responseBody!!

        assertThat(userBody.id).isNotNull()
        assertThat(userBody.email).isNotEmpty()
        assertThat(userBody.login).isNotEmpty()
        assertThat(userBody.name).isNotEmpty()
        assertThat(userBody.surname).isNotEmpty()
        assertThat(userBody.birthdate).isNotNull()
    }

    @Test
    fun `should not duplicate user in repository on repeated calls`() {
        // given
        val before = persistentUserRepository.count()

        // when
        repeat(3) { userAbility.callUserInfo().expectStatus().isOk }

        // then
        val after = persistentUserRepository.count()
        val diff = after - before
        assertThat(diff).isGreaterThanOrEqualTo(0)
        assertThat(diff).isLessThanOrEqualTo(1)
    }

    @Test
    fun `should return same user id across multiple calls`() {
        // given, when
        val ids = (1..3).map {
            val body = userAbility.callUserInfo().expectStatus().isOk.expectBody(UserResponse::class.java).returnResult().responseBody!!
            body.id
        }

        //then
        assertThat(ids.toSet().size).isEqualTo(1)
    }
}
