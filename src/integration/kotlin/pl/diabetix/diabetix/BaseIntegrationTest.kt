package pl.diabetix.diabetix

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.TestInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import pl.diabetix.diabetix.config.TestApplicationConfig
import pl.diabetix.diabetix.infrastructure.FixedTestDateTimeProvider
import java.time.OffsetDateTime

@SpringBootTest(
    classes = [DiabetixApplication::class, TestApplicationConfig::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
class BaseIntegrationTest {

    @Autowired
    private lateinit var repositories: List<MongoRepository<*, *>>

    @Autowired
    private lateinit var dateTimeProvider: FixedTestDateTimeProvider

    fun givenTime(dateTime: OffsetDateTime) {
        dateTimeProvider.setDateTime(dateTime)
    }

    // TODO:
    //  integrate keycloak

    companion object {
        private const val MONGO_DB_DOCKER_IMAGE = "mongo:7.0.5"
        private const val POSTGRES_DOCKER_IMAGE = "postgres:16.2"

        @JvmStatic
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer(POSTGRES_DOCKER_IMAGE)
            .withDatabaseName("keycloak")
            .withUsername("keycloak")
            .withPassword("keycloak")
            .withReuse(true)
            .apply { start() }

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
}
