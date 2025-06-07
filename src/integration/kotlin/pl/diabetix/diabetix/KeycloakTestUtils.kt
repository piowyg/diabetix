package pl.diabetix.diabetix

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import pl.diabetix.diabetix.KeycloakContainerInitializer.Companion.keycloakContainer
import pl.diabetix.diabetix.config.KeycloakTestProperties

@Component
class KeycloakTestUtils {
    @Autowired
    private lateinit var keycloakProperties: KeycloakTestProperties

    @Autowired
    private lateinit var restTemplate: RestTemplate

    fun getAccessToken(userName: String = "user", password: String = "user"): String {
        val tokenUrl = "${keycloakContainer.authServerUrl}/realms/${keycloakProperties.realm.name}/protocol/openid-connect/token"

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val form = LinkedMultiValueMap<String, String>()
        form.add("client_id", keycloakProperties.realm.client.id)
        form.add("client_secret", keycloakProperties.realm.client.secret)
        form.add("grant_type", "password")
        form.add("scope", "openid profile email offline_access")
        form.add("username", userName)
        form.add("password", password)

        val entity = HttpEntity(form, headers)

        val response = restTemplate.exchange(
            tokenUrl,
            HttpMethod.POST,
            entity,
            Map::class.java
        )

        if (response.statusCode.is2xxSuccessful) {
            return response.body?.get("access_token") as String
        } else {
            throw RuntimeException("Failed to obtain access token: $response")
        }
    }
}
