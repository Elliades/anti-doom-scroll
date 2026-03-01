package app.antidoomscroll.web

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

/**
 * Logs unhandled exceptions and returns a safe 500 body so the API doesn't expose stack traces.
 * Check backend logs for the actual cause.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResource(ex: NoResourceFoundException): ResponseEntity<Map<String, String>> {
        log.debug("No resource: {} {}", ex.httpMethod, ex.resourcePath)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to "Not found"))
    }

    @ExceptionHandler(Throwable::class)
    fun handleAny(ex: Throwable): ResponseEntity<Map<String, String>> {
        log.error("Unhandled exception; check backend logs", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(mapOf("error" to "Server error. Check backend logs."))
    }
}
