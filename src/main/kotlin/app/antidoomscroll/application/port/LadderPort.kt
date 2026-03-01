package app.antidoomscroll.application.port

import app.antidoomscroll.domain.LadderConfig

/**
 * Port: load ladder configuration.
 */
interface LadderPort {

    fun getByCode(code: String): LadderConfig?

    /** Returns summary info for all configured ladders. */
    fun listAll(): List<LadderSummary>

    data class LadderSummary(val code: String, val name: String?, val levelCount: Int)
}
