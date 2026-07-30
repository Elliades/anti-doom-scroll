package app.antidoomscroll.web.health

import com.fasterxml.jackson.annotation.JsonInclude

typealias CheckStatus = String // "ok" | "down" | "skipped"
typealias HealthStatus = String // "ok" | "degraded" | "down"

@JsonInclude(JsonInclude.Include.NON_NULL)
data class HealthCheck(
    val status: CheckStatus,
    val latencyMs: Long? = null,
    val detail: String? = null,
)

data class ServiceHealth(
    val status: HealthStatus,
    val service: String,
    val timestamp: String,
    val uptime: Double,
    val checks: Map<String, HealthCheck>,
)

/** Roll up check map → overall status. `skipped` checks are ignored. */
fun rollupHealth(checks: Map<String, HealthCheck>): HealthStatus {
    if (checks["frontend"]?.status == "down" || checks["backend"]?.status == "down") {
        return "down"
    }
    val required = checks.values.filter { it.status != "skipped" }
    if (required.any { it.status == "down" }) return "degraded"
    return "ok"
}

fun healthHttpStatus(status: HealthStatus): Int = if (status == "ok") 200 else 503
