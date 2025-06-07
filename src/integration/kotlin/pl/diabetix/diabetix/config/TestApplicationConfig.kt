package pl.diabetix.diabetix.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.web.client.RestTemplate
import pl.diabetix.diabetix.infrastructure.FixedTestDateTimeProvider
import java.time.Duration

// todo: sprawdzic co robi ten props
@TestConfiguration(proxyBeanMethods = false)
class TestApplicationConfig {

    @Bean
    @Primary
    fun dateTimeProvider() = FixedTestDateTimeProvider()


    // TODO:
    //  moze webClient?
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplateBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .readTimeout(Duration.ofSeconds(2))
            .build()
    }
}
