package pl.diabetix.diabetix.api.web.ability

import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient
import pl.diabetix.diabetix.BaseEndpointAbility

@Component
class UserEndpointAbility: BaseEndpointAbility() {

    fun callUserInfo(): WebTestClient.ResponseSpec {
        val headers = bearerAuth()
        return webClient
            .get()
            .uri(createUrl("/users/account"))
            .headers { it.addAll(headers) }
            .exchange()
    }
}
