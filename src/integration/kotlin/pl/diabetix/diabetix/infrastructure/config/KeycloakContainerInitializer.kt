package pl.diabetix.diabetix.infrastructure.config

import dasniko.testcontainers.keycloak.KeycloakContainer
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.io.ClassPathResource
import java.io.IOException
import java.util.*

class KeycloakContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        try {
            val properties = loadYamlProperties("application-integration.yml")

            val keycloakVersion = properties.getProperty("application.keycloak.version")

            val keycloakAdminUsername = properties.getProperty("application.keycloak.admin.username")
            val keycloakAdminPassword = properties.getProperty("application.keycloak.admin.password")
            val keycloakRealmName = properties.getProperty("application.keycloak.realm.name")

            // Start the Keycloak container
            keycloakContainer = KeycloakContainer("quay.io/keycloak/keycloak:$keycloakVersion")
                .withEnv("KEYCLOAK_ADMIN", keycloakAdminUsername)
                .withEnv("KEYCLOAK_ADMIN_PASSWORD", keycloakAdminPassword)
                .withRealmImportFile("realm-config/$keycloakRealmName-realm.json")

            keycloakContainer.start()

            // Dynamically set the properties in Spring's environment
            val issuerUri = keycloakContainer.authServerUrl + "/realms/" + keycloakRealmName

            TestPropertyValues.of(
                "keycloak.server.url=" + keycloakContainer.authServerUrl,
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=$issuerUri",
                "spring.security.oauth2.client.provider.oidc.issuer-uri=$issuerUri"
            ).applyTo(applicationContext.environment)
        } catch (e: IOException) {
            throw RuntimeException("Failed to load properties from YAML file", e)
        }
    }

    @Throws(IOException::class)
    private fun loadYamlProperties(filePath: String): Properties {
        val yamlPropertiesFactoryBean = YamlPropertiesFactoryBean()
        yamlPropertiesFactoryBean.setResources(ClassPathResource(filePath))
        return yamlPropertiesFactoryBean.getObject()
    }

    companion object {
        @JvmStatic
        lateinit var keycloakContainer: KeycloakContainer
    }
}