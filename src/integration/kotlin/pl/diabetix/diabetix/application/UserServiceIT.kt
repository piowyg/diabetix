package pl.diabetix.diabetix.application

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistration
import pl.diabetix.diabetix.BaseIntegrationTest
import pl.diabetix.diabetix.api.web.UserEndpointAbility

class UserServiceIT : BaseIntegrationTest() {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userEndpointAbility: UserEndpointAbility

    @Autowired
    private lateinit var authorizedClientService: OAuth2AuthorizedClientService

    @Autowired
    private lateinit var clientRegistration: ClientRegistration

    @Test
    fun `should create and sync user from OAuth2AuthenticationToken`() {
        val attributes = mapOf("login" to "integrationuser")
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val principal = object : org.springframework.security.oauth2.core.user.OAuth2User {
            override fun getAttributes() = attributes
            override fun getAuthorities() = authorities
            override fun getName() = attributes["login"] as String
        }
//        val authToken = OAuth2AuthenticationToken(principal, authorities, "registrationId")
//
//        val response = getToken()

//        TestSecurityContextHolder
//            .getContext().authentication = registerAuthenticationToken(
//            authorizedClientService,
//            clientRegistration,
//            testAuthenticationToken()
//        )
        userEndpointAbility.callUserInfo()

//        assertEquals("integrationuser", user.)
        // Możesz dodać dodatkowe asercje, np. sprawdzić obecność w bazie
    }
}
