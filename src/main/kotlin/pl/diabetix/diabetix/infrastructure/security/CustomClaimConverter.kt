package pl.diabetix.diabetix.infrastructure.security

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.jwt.MappedJwtClaimSetConverter
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration
import java.util.*
import java.util.stream.StreamSupport

/**
 * Claim converter to add custom claims by retrieving the user from the userinfo endpoint.
 */
class CustomClaimConverter(
    private val registration: ClientRegistration,
    private val webClient: WebClient) : Converter<Map<String?, Any>, Map<String, Any>> {

    private val bearerTokenResolver: BearerTokenResolver = DefaultBearerTokenResolver()
    private val delegate: MappedJwtClaimSetConverter = MappedJwtClaimSetConverter.withDefaults(emptyMap())
    private val users: Cache<String, ObjectNode> = Caffeine.newBuilder()
        .maximumSize(CACHE_SIZE)
        .expireAfterWrite(Duration.ofHours(1))
        .recordStats()
        .build()

    override fun convert(claims: Map<String?, Any>): Map<String, Any> {
        val convertedClaims = delegate.convert(claims)!!
        // Only look up user information if identity claims are missing
        if (claims.containsKey("groups") && claims.containsKey("birthdate")) {
            return convertedClaims
        }
        val attributes = RequestContextHolder.getRequestAttributes()
        if (attributes is ServletRequestAttributes) {
            // Retrieve and set the token
            val token = bearerTokenResolver.resolve(attributes.request)

            // Retrieve user info from OAuth provider if not already loaded
            val user = users.get(claims["sub"].toString()) { s ->
                val userInfo = webClient.get()
                    .uri(registration.providerDetails.userInfoEndpoint.uri)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    .retrieve()
                    .bodyToMono(ObjectNode::class.java)
                    .block()
                userInfo
            }

            // Add custom claims
            if (user != null) {
                convertedClaims["preferred_username"] = user["preferred_username"].asText()
                if (user.has("given_name")) {
                    convertedClaims["given_name"] = user["given_name"].asText()
                }
                if (user.has("family_name")) {
                    convertedClaims["family_name"] = user["family_name"].asText()
                }
                if (user.has("email")) {
                    convertedClaims["email"] = user["email"].asText()
                }
                // Allow full name in a name claim - happens with Auth0
                if (user.has("name")) {
                    val name =
                        user["name"].asText().split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (name.isNotEmpty()) {
                        convertedClaims["given_name"] = name[0]
                        convertedClaims["family_name"] =
                            java.lang.String.join(" ", *Arrays.copyOfRange(name, 1, name.size))
                    }
                }
                if (user.has("groups")) {
                    val groups =
                        StreamSupport.stream(user["groups"].spliterator(), false).map { obj: JsonNode -> obj.asText() }
                            .toList()
                    convertedClaims["groups"] = groups
                }
            }
        }
        return convertedClaims
    }

    companion object {
        private const val CACHE_SIZE = 10_000L
    }
}
