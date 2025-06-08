package pl.diabetix.diabetix.api.web.ability

import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import pl.diabetix.diabetix.BaseEndpointAbility
import pl.diabetix.diabetix.api.web.UserResponse

@Component
class UserEndpointAbility: BaseEndpointAbility() {

    fun callUserInfo(): ResponseEntity<UserResponse> {
        val httpEntity = headers()
        return restTemplate.exchange(
            createUrl("/users/account"),
            HttpMethod.GET,
            HttpEntity(null, httpEntity),
            UserResponse::class.java
        )
    }
}