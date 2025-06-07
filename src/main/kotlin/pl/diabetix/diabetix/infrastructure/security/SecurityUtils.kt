package pl.diabetix.diabetix.infrastructure.security

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.util.*
import java.util.stream.Collectors

object SecurityUtils {
    fun extractAuthorityFromClaims(claims: Map<String, Any>) =
        mapRolesToGrantedAuthorities(getRolesFromClaims(claims))

    fun getCurrentUserLogin(): Optional<String> {
        val securityContext = SecurityContextHolder.getContext()
        return Optional.ofNullable<String>(extractPrincipal(securityContext.authentication))
    }

    private fun extractPrincipal(authentication: Authentication?): String? {
        if (authentication == null) {
            return null
        } else if (authentication.principal is UserDetails) {
            return (authentication.principal as UserDetails).username
        } else if (authentication is JwtAuthenticationToken) {
            return authentication.token.claims["preferred_username"] as String?
        } else if (authentication.principal is DefaultOidcUser) {
            val attributes = (authentication.principal as DefaultOidcUser).attributes
            if (attributes.containsKey("preferred_username")) {
                return attributes["preferred_username"] as String?
            }
        } else if (authentication.principal is String) {
            return authentication.principal as String
        }
        return null
    }
    private fun mapRolesToGrantedAuthorities(roles: List<String>): List<GrantedAuthority> =
        roles.stream().filter { role -> role.startsWith("ROLE_") }
            .map { role -> SimpleGrantedAuthority(role) }.collect(
                Collectors.toList()
            )

    @Suppress("UNCHECKED_CAST")
    private fun getRolesFromClaims(claims: Map<String, Any>) =
       claims.getOrDefault("groups", emptyList<String>()) as List<String>
}
