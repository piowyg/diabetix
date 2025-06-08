package pl.diabetix.diabetix.api.web

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import pl.diabetix.diabetix.BaseIntegrationTest
import pl.diabetix.diabetix.api.web.ability.UserEndpointAbility

class UserControllerSpec : BaseIntegrationTest() {

    @Autowired
    lateinit var userAbility: UserEndpointAbility

    @Test
    fun `should return user account for authenticated user`() {
        val response = userAbility.callUserInfo()

        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
    }
}