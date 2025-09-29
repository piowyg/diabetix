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
import java.time.LocalDate

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
        // given
        val attributes = mapOf("login" to "testuser")
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val principal = mock<org.springframework.security.oauth2.core.user.OAuth2User> {
            on { this.attributes } doReturn attributes
        }
        val authToken = OAuth2AuthenticationToken(principal, authorities, "registrationId")
        val user = UserBuilder().login("testuser").build()
        userRepository.given(user)
        whenever(userDataFactory.createUser(any())).thenReturn(user)

        // when
        val result = underTest.getUserFromAuthentication(authToken)

        // then
        assertEquals(user, result)
    }

    @Test
    fun `should return user from JwtAuthenticationToken`() {
        // given
        val jwt = mock<Jwt> {
            on { claims } doReturn mapOf("login" to "jwtuser")
        }
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val authToken = JwtAuthenticationToken(jwt, authorities)
        val user = UserBuilder().login("jwtuser").build()
        whenever(userDataFactory.createUser(any())).thenReturn(user)
        userRepository.given(user)

        // when
        val result = underTest.getUserFromAuthentication(authToken)


        // then
        assertEquals(user, result)
    }

    @Test
    fun `createUser should build user from basic claims without uid`() {
        // given
        val factory = UserDataFactory()
        val details = mapOf(
            "sub" to "123",
            "preferred_username" to "John.DOE",
            "given_name" to "John",
            "family_name" to "Doe",
            "birthdate" to "2000-01-02",
            "email" to "John.Doe@Example.COM",
            "email_verified" to true
        )

        // when
        val user = factory.createUser(details)

        // then
        assertEquals("123", user.id)
        assertEquals("john.doe", user.login) // preferred_username lowercased
        assertEquals("john.doe@example.com", user.email) // email lowercased
        assertEquals("John", user.name)
        assertEquals("Doe", user.surname)
        assertEquals(LocalDate.parse("2000-01-02"), user.birthdate)
        assertTrue(user.activated)
    }

    @Test
    fun `createUser should use uid as id and sub as login when uid present`() {
        // given
        val factory = UserDataFactory()
        val details = mapOf(
            "sub" to "User@Example.com",
            "uid" to "999",
            "preferred_username" to "ignored_if_uid_present",
            "given_name" to "Alice",
            "family_name" to "Smith",
            "birthdate" to "1999-12-31",
            "email" to "Alice.Smith@Example.com",
            "email_verified" to false
        )

        // when
        val user = factory.createUser(details)

        // then
        assertEquals("999", user.id) // uid wins
        assertEquals("User@Example.com", user.login) // login forced to sub when uid present
        assertEquals("alice.smith@example.com", user.email) // email lowercased
        assertEquals("Alice", user.name)
        assertEquals("Smith", user.surname)
        assertEquals(LocalDate.parse("1999-12-31"), user.birthdate)
        assertFalse(user.activated) // from email_verified=false
    }

    @Test
    fun `createUser should fallback to name when given_name missing`() {
        // given
        val factory = UserDataFactory()
        val details = mapOf(
            "sub" to "abc",
            "preferred_username" to "abc",
            "name" to "Bob",
            "family_name" to "Brown",
            "birthdate" to "1980-05-20",
            "email" to "bob.brown@example.com"
        )

        // when
        val user = factory.createUser(details)

        // then
        assertEquals("Bob", user.name) // fallback from name
        assertEquals("Brown", user.surname)
    }

    @Test
    fun `createUser should fallback login to id when preferred_username missing`() {
        // given
        val factory = UserDataFactory()
        val details = mapOf(
            "sub" to "sub-xyz",
            // no preferred_username
            "given_name" to "Eve",
            "family_name" to "White",
            "birthdate" to "1970-01-01",
            "email" to "eve.white@example.com"
        )

        // when
        val user = factory.createUser(details)

        // then
        assertEquals("sub-xyz", user.login) // login defaults to id when preferred_username null
    }

    @Test
    fun `createUser should set email to login for Auth0 style sub when email missing and login is email`() {
        // given
        val factory = UserDataFactory()
        val details = mapOf(
            "sub" to "auth0|123456",
            "preferred_username" to "USER@Example.com", // will be lowercased and used as login
            "given_name" to "Tom",
            "family_name" to "Hanks",
            "birthdate" to "1960-07-09"
            // email absent
        )

        // when
        val user = factory.createUser(details)

        // then
        assertEquals("user@example.com", user.login)
        assertEquals("user@example.com", user.email) // email should equal login when sub contains | and login contains @
    }

    @Test
    fun `createUser should fallback email to sub when email missing and not auth0 condition`() {
        // given
        val factory = UserDataFactory()
        val details = mapOf(
            "sub" to "sub-only",
            // preferred_username missing -> login=id=sub-only
            "given_name" to "Mark",
            "family_name" to "Twain",
            "birthdate" to "1955-11-12"
        )

        // when
        val user = factory.createUser(details)

        // then
        assertEquals("sub-only", user.email)
        assertEquals("sub-only", user.login)
    }

    @Test
    fun `createUser should throw when family_name missing`() {
        // given
        val factory = UserDataFactory()
        val details = mapOf(
            "sub" to "x",
            "given_name" to "John",
            // family_name missing
            "birthdate" to "2001-01-01",
            "email" to "j@example.com"
        )

        // when, then
        assertThrows(IllegalStateException::class.java) {
            factory.createUser(details)
        }
    }

    @Test
    fun `createUser should throw when birthdate missing`() {
        // given
        val factory = UserDataFactory()
        val details = mapOf(
            "sub" to "x",
            "given_name" to "John",
            "family_name" to "Doe",
            // birthdate missing
            "email" to "j@example.com"
        )

        assertThrows(IllegalStateException::class.java) {
            factory.createUser(details)
        }
    }

    @Test
    fun `createUser should throw when name and given_name both missing`() {
        val factory = UserDataFactory()
        val details = mapOf(
            "sub" to "x",
            // no given_name
            // no name
            "family_name" to "Doe",
            "birthdate" to "2001-01-01",
            "email" to "j@example.com"
        )

        assertThrows(IllegalStateException::class.java) {
            factory.createUser(details)
        }
    }
}
