package pl.diabetix.diabetix.infrastructure.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.diabetix.diabetix.domain.DefaultDateTimeProvider
import pl.diabetix.diabetix.infrastructure.ApplicationProperties

@Configuration
@EnableConfigurationProperties(value = [ApplicationProperties::class])
class ApplicationConfig {

    @Bean
    fun dateTimeProvider() = DefaultDateTimeProvider("UTC")
}