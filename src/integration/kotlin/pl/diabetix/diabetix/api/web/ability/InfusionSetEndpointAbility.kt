package pl.diabetix.diabetix.api.web.ability

import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient
import pl.diabetix.diabetix.BaseEndpointAbility
import pl.diabetix.diabetix.api.web.InfusionSetRequest

@Component
class InfusionSetEndpointAbility: BaseEndpointAbility() {

    fun callCreateInfusionSet(infusionSetRequest: InfusionSetRequest): WebTestClient.ResponseSpec {
        val httpEntity = bearerAuth()
        return webClient
            .post()
            .uri(createUrl("/infusion-sets"))
            .headers { it.addAll(httpEntity) }
            .bodyValue(infusionSetRequest)
            .exchange()
    }
}
