package pl.diabetix.diabetix.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.web.client.RestTemplate
import pl.diabetix.diabetix.infrastructure.FixedTestDateTimeProvider

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
        return RestTemplate()
    }
}
