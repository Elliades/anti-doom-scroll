package app.antidoomscroll.application.port

import app.antidoomscroll.domain.Journey

/**
 * Port: load journey definition by code. Enables config- or DB-driven journeys; add steps without code change.
 */
interface JourneyPort {

    /**
     * Returns the journey for the given code (e.g. "default"), or null if not found.
     */
    fun getByCode(code: String): Journey?
}
