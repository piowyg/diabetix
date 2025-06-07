package pl.diabetix.diabetix.api.web

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import pl.diabetix.diabetix.KeycloakTestUtils
//import pl.diabetix.diabetix.AuthenticationUtils
import pl.diabetix.diabetix.UrlUtils


@Component
class UserEndpointAbility(
    private val restTemplate: RestTemplate, // TODO: change to WebClient or something
    private val urlUtils: UrlUtils,
    private val keycloakTestUtils: KeycloakTestUtils
) {

    fun callUserInfo(): ResponseEntity<UserResponse> {
        val httpEntity = headers()
        return restTemplate.exchange(
            urlUtils.createUrl("/users/account"),
            org.springframework.http.HttpMethod.GET,
            HttpEntity(null, httpEntity),
            UserResponse::class.java
        )
    }

    private fun headers(): HttpHeaders {
        val headers = HttpHeaders()
        headers.setBearerAuth(keycloakTestUtils.getAccessToken())
        headers.contentType = MediaType.APPLICATION_JSON
        return headers
    }
}