package pl.diabetix.diabetix.infrastructure.config

import io.github.oshai.kotlinlogging.KotlinLogging
import io.netty.channel.ChannelOption
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
class WebClientConfig {

    @Bean
    fun keycloakWebClient(builder: WebClient.Builder): WebClient {
        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .responseTimeout(Duration.ofSeconds(5))
            .doOnConnected { conn ->
                conn.addHandlerLast(ReadTimeoutHandler(5, TimeUnit.SECONDS))
                    .addHandlerLast(WriteTimeoutHandler(5, TimeUnit.SECONDS))
            }

        return builder
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .filter(logRequest())
            .filter(logResponse())
            .build()
    }

    private fun logRequest(): ExchangeFilterFunction = ExchangeFilterFunction.ofRequestProcessor { clientRequest ->
        logger.info { "Sending request: ${clientRequest.method()} ${clientRequest.url()}" }
        clientRequest.headers()
            .forEach { name, values -> values.forEach { value -> logger.debug{"$name: $value"} } }
        reactor.core.publisher.Mono.just(clientRequest)
    }

    private fun logResponse(): ExchangeFilterFunction = ExchangeFilterFunction.ofResponseProcessor { clientResponse ->
        logger.info { "Handling response: status=${clientResponse.statusCode()}" }
        reactor.core.publisher.Mono.just(clientResponse)
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
