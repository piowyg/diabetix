package pl.diabetix.diabetix.application

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.*
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import pl.diabetix.diabetix.UserBuilder
import pl.diabetix.diabetix.fake.FakeUserRepository

class UserServiceTest {
    private lateinit var userRepository: FakeUserRepository
    private lateinit var userDataFactory: UserDataFactory
    private lateinit var underTest: UserService

    @BeforeEach
    fun setUp() {
        userRepository = FakeUserRepository()
        userDataFactory = mock<UserDataFactory>()
        underTest = UserService(userRepository, userDataFactory)
    }

    @Test
    fun `should return user from OAuth2AuthenticationToken`() {
        val attributes = mapOf("login" to "testuser")
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val principal = mock<org.springframework.security.oauth2.core.user.OAuth2User> {
            on { this.attributes } doReturn attributes
        }
        val authToken = OAuth2AuthenticationToken(principal, authorities, "registrationId")
        val user = UserBuilder().login("testuser").build()
        userRepository.given(user)
        whenever(userDataFactory.createUser(any())).thenReturn(user)

        val result = underTest.getUserFromAuthentication(authToken)

        assertEquals(user, result)
    }

    @Test
    fun `should return user from JwtAuthenticationToken`() {
        val jwt = mock<Jwt> {
            on { claims } doReturn mapOf("login" to "jwtuser")
        }
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val authToken = JwtAuthenticationToken(jwt, authorities)
        val user = UserBuilder().login("jwtuser").build()
        whenever(userDataFactory.createUser(any())).thenReturn(user)
        userRepository.given(user)

        val result = underTest.getUserFromAuthentication(authToken)

        assertEquals(user, result)
    }
}
