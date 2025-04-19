package pl.diabetix.diabetix.api.web

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import pl.diabetix.diabetix.domain.CreateUserException

@ControllerAdvice
class ExceptionHandlers() {


    @ExceptionHandler
    fun handleCreateUserExceptionn(ex: CreateUserException): ResponseEntity<ErrorResponse> {
        logger.error { "handle CreateUserException ${ex.message}" }
        return handleException(ex.message, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        logger.error { "handle RuntimeException ${ex.message}" }
        return handleException(ex.message, HttpStatus.INTERNAL_SERVER_ERROR)
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