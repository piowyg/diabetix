package pl.diabetix.diabetix.infrastructure.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import pl.diabetix.diabetix.domain.DefaultDateTimeProvider
import pl.diabetix.diabetix.infrastructure.ApplicationProperties

@Configuration
@EnableConfigurationProperties(value = [ApplicationProperties::class])
class ApplicationConfig {

    @Bean
    fun dateTimeProvider() = DefaultDateTimeProvider()

    @Primary
    @Bean
    fun objectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.registerModule(KotlinModule.Builder().build())
        objectMapper.registerModule(JavaTimeModule())
        return objectMapper
    }
}