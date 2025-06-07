package pl.diabetix.diabetix.infrastructure.security

import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component
import java.util.*

/**
 * Implementation of [AuditorAware] based on Spring Security.
 */
@Component
class SpringSecurityAuditorAware : AuditorAware<String> {

    override fun getCurrentAuditor(): Optional<String> {
        return Optional.of(SecurityUtils.getCurrentUserLogin().orElse(SYSTEM))
    }

    companion object {
        private const val SYSTEM = "system"
    }
}
