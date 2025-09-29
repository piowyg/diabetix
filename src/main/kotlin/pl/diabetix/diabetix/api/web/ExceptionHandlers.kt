package pl.diabetix.diabetix.api.web

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import pl.diabetix.diabetix.domain.CreateUserException
import pl.diabetix.diabetix.domain.InfusionSetNotFoundException
import pl.diabetix.diabetix.domain.InvalidInfusionSetUpdateException
import pl.diabetix.diabetix.domain.UserNotFoundException

@ControllerAdvice
class ExceptionHandlers() {

    @ExceptionHandler
    fun handleCreateUserException(ex: CreateUserException): ResponseEntity<ErrorResponse> {
        logger.error { "handle CreateUserException ${ex.message}" }
        return handleException(ex.message, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        logger.error { "handle RuntimeException ${ex.message}" }
        return handleException(ex.message, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler
    fun handleUserNotFoundException(ex: UserNotFoundException): ResponseEntity<ErrorResponse> {
        logger.error { "handle UserNotFoundException ${ex.message}" }
        return handleException(ex.message, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler
    fun handleInfusionSetNotFoundException(ex: InfusionSetNotFoundException): ResponseEntity<ErrorResponse> {
        logger.error { "handle InfusionSetNotFoundException ${ex.message}" }
        return handleException(ex.message, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler
    fun handleInvalidInfusionSetUpdateException(ex: InvalidInfusionSetUpdateException): ResponseEntity<ErrorResponse> {
        logger.error { "handle InvalidInfusionSetUpdateException ${ex.message}" }
        return handleException(ex.message, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler
    fun handleInvalidInfusionSetUpdateException(ex: IllegalStateException): ResponseEntity<ErrorResponse> {
        logger.error { "handle IllegalStateException ${ex.message}" }
        return handleException(ex.message, HttpStatus.BAD_REQUEST)
    }

    private fun handleException(message: String?, status: HttpStatus) =
        ResponseEntity(ErrorResponse(message ?: INTERNAL_ERROR, status.value()), status)

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val INTERNAL_ERROR = "INTERNAL ERROR"
    }
}

data class ErrorResponse(
    val message: String,
    val status: Int
)
