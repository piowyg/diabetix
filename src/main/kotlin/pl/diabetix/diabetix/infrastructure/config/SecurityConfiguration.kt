package pl.diabetix.diabetix.infrastructure.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.oidc.StandardClaimNames
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
import org.springframework.web.reactive.function.client.WebClient
import pl.diabetix.diabetix.infrastructure.ApplicationProperties
import pl.diabetix.diabetix.infrastructure.security.CustomClaimConverter
import pl.diabetix.diabetix.infrastructure.security.SecurityUtils

@Configuration
@EnableMethodSecurity(securedEnabled = true)
class SecurityConfiguration(
    @Value("\${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    val issuerUri: String,
    val applicationProperties: ApplicationProperties
) {

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors(Customizer.withDefaults<CorsConfigurer<HttpSecurity>>())
            .csrf {
                it.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            }
            .headers {
                it
                    .contentSecurityPolicy { csp -> csp.policyDirectives(applicationProperties.security.contentSecurityPolicy) }
                    .frameOptions { obj -> obj.sameOrigin() }
                    .referrerPolicy{ referrer -> referrer
                        .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                    }
                    .permissionsPolicyHeader { permissions ->
                        permissions.policy(applicationProperties.security.permissionsPolicyHeader)
                    }
            }
            .authorizeHttpRequests {
                it.requestMatchers("/users/account").authenticated()
                it.requestMatchers("/infusion-sets").authenticated()
                it.anyRequest().permitAll()
            }
            .oauth2Login { oauth2 -> oauth2.loginPage("/").userInfoEndpoint { userInfo -> userInfo.oidcUserService(oidcUserService()) } }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt {
                    jwt ->
                        jwt.jwtAuthenticationConverter(authenticationConverter())
                }
            }
        return http.build()
    }

    fun authenticationConverter(): Converter<Jwt, AbstractAuthenticationToken> {
        val jwtAuthenticationConverter = JwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter { jwt ->
            SecurityUtils.extractAuthorityFromClaims(jwt.claims)
        }
        jwtAuthenticationConverter.setPrincipalClaimName(StandardClaimNames.PREFERRED_USERNAME)
        return jwtAuthenticationConverter
    }

    fun oidcUserService(): OAuth2UserService<OidcUserRequest, OidcUser> {
        val delegate = OidcUserService()

        return OAuth2UserService { userRequest ->
            val oidcUser = delegate.loadUser(userRequest)
            DefaultOidcUser(
                oidcUser.authorities,
                oidcUser.idToken,
                oidcUser.userInfo,
                StandardClaimNames.PREFERRED_USERNAME
            )
        }
    }

    @Bean
    fun jwtDecoder(
        clientRegistrationRepository: ClientRegistrationRepository,
        keycloakWebClient: WebClient
    ): JwtDecoder {
        val jwtDecoder = JwtDecoders.fromOidcIssuerLocation<NimbusJwtDecoder>(issuerUri)

        val withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri)
        val withAudience: OAuth2TokenValidator<Jwt> = DelegatingOAuth2TokenValidator(withIssuer)

        jwtDecoder.setJwtValidator(withAudience)
        jwtDecoder.setClaimSetConverter(
            CustomClaimConverter(
                registration = clientRegistrationRepository.findByRegistrationId("oidc"),
                webClient = keycloakWebClient
            )
        )

        return jwtDecoder
    }
}
