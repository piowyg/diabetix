package pl.diabetix.diabetix.infrastructure

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * Properties specific to Diabetix.
 *
 *
 * Properties are configured in the `application.yml` file.
 */
@ConfigurationProperties(prefix = "diabetix", ignoreUnknownFields = false)
data class ApplicationProperties(
    @NestedConfigurationProperty
    val security: Security = Security()
)

data class OAuth2(
    var audience: List<String> = mutableListOf()
)

class Security {
    var contentSecurityPolicy = ""
    var permissionsPolicyHeader = ""
    var oauth2 = OAuth2()
}
