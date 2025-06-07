package pl.diabetix.diabetix.infrastructure.config

import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.junit.jupiter.Testcontainers
import pl.diabetix.diabetix.DiabetixApplication
import pl.diabetix.diabetix.config.TestApplicationConfig

/**
 * Base composite annotation for integration tests.
 */
@ActiveProfiles("integration")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
    classes = [DiabetixApplication::class, TestApplicationConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [KeycloakContainerInitializer::class])
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
annotation class IntegrationTest