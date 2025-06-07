package pl.diabetix.diabetix.infrastructure.config

import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.context.annotation.*
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

@Configuration
@EnableMongoRepositories(
    basePackages = ["pl.diabetix.diabetix.infrastructure.adapter.mongodb"],
    includeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = arrayOf(MongoRepository::class))]
)
@Import(value = [MongoAutoConfiguration::class])
@EnableMongoAuditing(auditorAwareRef = "springSecurityAuditorAware")
class DatabaseConfiguration {
    @Bean
    fun validatingMongoEventListener(): ValidatingMongoEventListener {
        return ValidatingMongoEventListener(validator())
    }

    @Bean
    fun validator(): LocalValidatorFactoryBean {
        return LocalValidatorFactoryBean()
    }
}
