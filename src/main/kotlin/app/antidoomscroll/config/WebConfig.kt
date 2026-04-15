package app.antidoomscroll.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    @Value("\${app.cors.origins:http://localhost:5173}") private val corsOrigins: String
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        val origins = corsOrigins.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        val mapping = registry.addMapping("/api/**")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowCredentials(true)

        if (origins.any { it == "*" }) {
            mapping.allowedOriginPatterns("*")
            return
        }

        // Allow any localhost port so local dev works regardless of which port
        // Vite binds to (5174, 5175, … when the preferred port is already taken).
        val hasLocalhostWildcard = origins.any { it.matches(Regex("https?://localhost.*")) }

        if (hasLocalhostWildcard) {
            // allowedOriginPatterns supports wildcards; use it when localhost is configured
            mapping.allowedOriginPatterns(*origins
                .map { if (it.startsWith("http://localhost") || it.startsWith("https://localhost")) "http://localhost:*" else it }
                .distinct()
                .toTypedArray()
            )
        } else {
            mapping.allowedOrigins(*origins.toTypedArray())
        }
    }
}
