package pl.diabetix.diabetix.application

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import pl.diabetix.diabetix.domain.Authority
import pl.diabetix.diabetix.domain.User
import pl.diabetix.diabetix.domain.UserRepository
import java.util.stream.Collectors

@Component
class UserService(
    private val userRepository: UserRepository,
    private val userDataFactory: UserDataFactory
) {
    /**
     * Returns the user from an OAuth 2.0 login or resource server with JWT.
     * Synchronizes the user in the local repository.
     *
     * @param authToken the authentication token.
     * @return the user from the authentication.
     */
    fun getUserFromAuthentication(authToken: AbstractAuthenticationToken): User {
        val attributes: MutableMap<String, Any> = when (authToken) {
            is OAuth2AuthenticationToken -> authToken.principal.attributes.toMutableMap()
            is JwtAuthenticationToken -> authToken.tokenAttributes.toMutableMap()
            else -> throw IllegalArgumentException("AuthenticationToken is not OAuth2 or JWT!")
        }
        val authorities = authToken
            .authorities
            .stream()
            .map(GrantedAuthority::getAuthority)
            .map { Authority(it) }
            .collect(Collectors.toSet())
        attributes["authorities"] = authorities

        val user = userDataFactory.getUser(attributes)
        return syncUserWithIdP(user)
    }

    private fun syncUserWithIdP(user: User): User {
        // save account in to sync users between IdP and JHipster's local database
        val existingUser = userRepository.findUserByLogin(user.login)
        if (existingUser == null) {
            logger.debug{ "Saving user ${user.login} in local database" }
            userRepository.create(user)
        } else if (existingUser != user) {
            logger.debug{ "Updating user ${user.login} in local database" }
            updateUser(user)
        }

        return user
    }

    private fun updateUser(user: User) {
        userRepository.update(user)
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
