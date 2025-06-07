package pl.diabetix.diabetix

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.TestInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import pl.diabetix.diabetix.infrastructure.config.KeycloakContainerInitializer.Companion.keycloakContainer
import pl.diabetix.diabetix.infrastructure.FixedTestDateTimeProvider
import pl.diabetix.diabetix.infrastructure.config.IntegrationTest
import java.time.OffsetDateTime


/**
 * Base class for integration tests.
 * Sets up the test environment, including MongoDB and Keycloak containers.
 * Provides utilities for time management in tests and repository cleanup.
 */
@IntegrationTest
class BaseIntegrationTest {

    @Autowired
    private lateinit var repositories: List<MongoRepository<*, *>>

    @Autowired
    private lateinit var dateTimeProvider: FixedTestDateTimeProvider

    @Autowired
    private  lateinit var keycloakTestUtils: KeycloakTestUtils

    fun getAccessToken(): String {
        return keycloakTestUtils.getAccessToken()
    }

    fun givenTime(dateTime: OffsetDateTime) {
        dateTimeProvider.setDateTime(dateTime)
    }

    // TODO:
    //  integrate keycloak

    companion object {
        private const val MONGO_DB_DOCKER_IMAGE = "mongo:8.0.4"

        @JvmStatic
        val mongo: MongoDBContainer = MongoDBContainer(
            DockerImageName.parse(MONGO_DB_DOCKER_IMAGE)
                .asCompatibleSubstituteFor("mongo")
        ).withReuse(true)
            .apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.mongodb.uri") { mongo.replicaSetUrl }
        }
    }

    @AfterEach
    fun cleanUp(info: TestInfo) {
        if (info.tags.contains("cleanRepo"))  {
            repositories.forEach { it.deleteAll() }
        }
    }

    @AfterAll
fun tearDownKeycloak() {
        keycloakContainer.stop()
    }
}
