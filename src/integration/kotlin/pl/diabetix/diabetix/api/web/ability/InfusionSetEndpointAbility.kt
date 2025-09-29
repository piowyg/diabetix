package pl.diabetix.diabetix.api.web.ability

import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient
import pl.diabetix.diabetix.BaseEndpointAbility
import pl.diabetix.diabetix.api.web.InfusionSetCreateRequest
import pl.diabetix.diabetix.api.web.InfusionSetUpdateRequest

@Component
class InfusionSetEndpointAbility: BaseEndpointAbility() {

    fun callCreateInfusionSet(request: InfusionSetCreateRequest): WebTestClient.ResponseSpec {
        val httpEntity = bearerAuth()
        return webClient
            .post()
            .uri(createUrl("/infusion-sets"))
            .headers { it.addAll(httpEntity) }
            .bodyValue(request)
            .exchange()
    }

    fun callGetInfusionSetsByUser(userId: String): WebTestClient.ResponseSpec {
        val httpEntity = bearerAuth()
        return webClient
            .get()
            .uri(createUrl("/infusion-sets/user/$userId"))
            .headers { it.addAll(httpEntity) }
            .exchange()
    }

    fun callGetInfusionSet(id: String): WebTestClient.ResponseSpec {
        val httpEntity = bearerAuth()
        return webClient
            .get()
            .uri(createUrl("/infusion-sets/$id"))
            .headers { it.addAll(httpEntity) }
            .exchange()
    }

    fun callUpdateInfusionSet(id: String, request: InfusionSetUpdateRequest): WebTestClient.ResponseSpec {
        val httpEntity = bearerAuth()
        return webClient
            .put()
            .uri(createUrl("/infusion-sets/$id"))
            .headers { it.addAll(httpEntity) }
            .bodyValue(request)
            .exchange()
    }
}
