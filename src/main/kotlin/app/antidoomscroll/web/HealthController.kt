package app.antidoomscroll.web

import app.antidoomscroll.web.health.HealthCheck
import app.antidoomscroll.web.health.ServiceHealth
import app.antidoomscroll.web.health.healthHttpStatus
import app.antidoomscroll.web.health.rollupHealth
import org.springframework.core.io.ClassPathResource
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.management.ManagementFactory
import java.time.Instant

@RestController
class HealthController(
    private val jdbcTemplate: JdbcTemplate,
) {

    @GetMapping("/api/health")
    fun health(): ResponseEntity<ServiceHealth> {
        val checks = linkedMapOf(
            "frontend" to checkFrontend(),
            "backend" to HealthCheck(status = "ok"),
            "database" to pingDatabase(),
        )
        val status = rollupHealth(checks)
        val body = ServiceHealth(
            status = status,
            service = SERVICE_NAME,
            timestamp = Instant.now().toString(),
            uptime = ManagementFactory.getRuntimeMXBean().uptime / 1000.0,
            checks = checks,
        )
        return ResponseEntity.status(healthHttpStatus(status)).body(body)
    }

    private fun checkFrontend(): HealthCheck {
        val index = ClassPathResource("static/index.html")
        return if (index.exists() && index.isReadable) {
            HealthCheck(status = "ok")
        } else {
            HealthCheck(status = "down", detail = "static/index.html missing")
        }
    }

    private fun pingDatabase(): HealthCheck {
        val started = System.currentTimeMillis()
        return try {
            jdbcTemplate.queryForObject("SELECT 1", Int::class.java)
            HealthCheck(status = "ok", latencyMs = System.currentTimeMillis() - started)
        } catch (ex: Exception) {
            HealthCheck(
                status = "down",
                latencyMs = System.currentTimeMillis() - started,
                detail = ex.message ?: "database unreachable",
            )
        }
    }

    companion object {
        const val SERVICE_NAME = "anti-doom-scroll"
    }
}
