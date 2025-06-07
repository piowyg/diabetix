package pl.diabetix.diabetix.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import pl.diabetix.diabetix.BaseIntegrationTest
import pl.diabetix.diabetix.api.web.UserEndpointAbility

class UserControllerSpec : BaseIntegrationTest() {

    @Autowired
    lateinit var userAbility: UserEndpointAbility

    @Test
    fun `should return user account for authenticated user`() {
        val response = userAbility.callUserInfo()

        assertEquals(HttpStatus.OK, response.statusCode)
    }
}

