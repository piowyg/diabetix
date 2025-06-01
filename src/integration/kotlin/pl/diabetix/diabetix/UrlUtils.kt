package pl.diabetix.diabetix

import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class UrlUtils(
    private val environment: Environment
) {

    fun createUrl(endpoint: String) = "http://localhost:${environment.getProperty("local.server.port")}${endpoint}"
}