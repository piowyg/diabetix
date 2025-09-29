package pl.diabetix.diabetix

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient

@Component
class BaseEndpointAbility(){

    @Autowired
    lateinit var webClient: WebTestClient
    @Autowired
    private lateinit var urlUtils: UrlUtils
    @Autowired
    private lateinit var keycloakTestUtils: KeycloakTestUtils

    fun createUrl(path: String): String {
        return urlUtils.createUrl(path)
    }

    fun bearerAuth(userName: String = "user", password: String = "user"): HttpHeaders {
        val headers = HttpHeaders()
        headers.setBearerAuth(keycloakTestUtils.getAccessToken(userName, password))
        headers.contentType = MediaType.APPLICATION_JSON
        return headers
    }
}