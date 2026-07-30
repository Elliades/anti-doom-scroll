package app.antidoomscroll.web.health

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ServiceHealthTest {

    @Test
    fun `should be ok when all required checks are ok`() {
        assertEquals(
            "ok",
            rollupHealth(
                mapOf(
                    "frontend" to HealthCheck(status = "ok"),
                    "backend" to HealthCheck(status = "ok"),
                    "database" to HealthCheck(status = "ok", latencyMs = 2),
                ),
            ),
        )
    }

    @Test
    fun `should be degraded when database is down but app process is up`() {
        assertEquals(
            "degraded",
            rollupHealth(
                mapOf(
                    "frontend" to HealthCheck(status = "ok"),
                    "backend" to HealthCheck(status = "ok"),
                    "database" to HealthCheck(status = "down", detail = "connection refused"),
                ),
            ),
        )
    }

    @Test
    fun `should be down when backend is down`() {
        assertEquals(
            "down",
            rollupHealth(
                mapOf(
                    "frontend" to HealthCheck(status = "ok"),
                    "backend" to HealthCheck(status = "down"),
                    "database" to HealthCheck(status = "skipped"),
                ),
            ),
        )
    }

    @Test
    fun `should ignore skipped checks`() {
        assertEquals(
            "ok",
            rollupHealth(
                mapOf(
                    "frontend" to HealthCheck(status = "ok"),
                    "backend" to HealthCheck(status = "ok"),
                    "redis" to HealthCheck(status = "skipped"),
                ),
            ),
        )
    }

    @Test
    fun `should map ok to 200 and others to 503`() {
        assertEquals(200, healthHttpStatus("ok"))
        assertEquals(503, healthHttpStatus("degraded"))
        assertEquals(503, healthHttpStatus("down"))
    }
}
