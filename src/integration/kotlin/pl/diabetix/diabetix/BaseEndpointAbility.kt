package pl.diabetix.diabetix

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class BaseEndpointAbility{
    @Autowired
    lateinit var restTemplate: RestTemplate // TODO: change to WebClient or something
    @Autowired
    private lateinit var urlUtils: UrlUtils
    @Autowired
    private lateinit var keycloakTestUtils: KeycloakTestUtils

    fun createUrl(path: String): String {
        return urlUtils.createUrl(path)
    }

    fun headers(userName: String = "user", password: String = "user"): HttpHeaders {
        val headers = HttpHeaders()
        headers.setBearerAuth(keycloakTestUtils.getAccessToken(userName, password))
        headers.contentType = MediaType.APPLICATION_JSON
        return headers
    }
}