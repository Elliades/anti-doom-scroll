package app.antidoomscroll.infrastructure.config

import app.antidoomscroll.application.port.JourneyPort
import app.antidoomscroll.application.port.ReflectionContentPort
import app.antidoomscroll.config.JourneyConfigProperties
import app.antidoomscroll.domain.Journey
import org.springframework.stereotype.Component

/**
 * Config-driven implementation of JourneyPort and ReflectionContentPort.
 * Journey and reflection content come from application.yml (app.journey).
 * Always registered so GET /api/journey works; uses defaults when config is missing.
 */
@Component
class JourneyConfigAdapter(
    private val properties: JourneyConfigProperties
) : JourneyPort, ReflectionContentPort {

    private val journey: Journey by lazy { properties.toJourney() }

    override fun getByCode(code: String): Journey? =
        if (code == journey.code) journey else null

    override fun getByKey(contentKey: String): ReflectionContent? {
        val c = properties.content[contentKey] ?: return null
        return ReflectionContent(title = c.title, body = c.body)
    }
}
