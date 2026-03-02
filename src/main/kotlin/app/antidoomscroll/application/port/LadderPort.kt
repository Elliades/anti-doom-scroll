package app.antidoomscroll.application.port

import app.antidoomscroll.domain.LadderConfig

/**
 * Port: load ladder configuration.
 */
interface LadderPort {

    fun getByCode(code: String): LadderConfig?

    /** Returns summary info for all configured ladders. */
    fun listAll(): List<LadderSummary>

    /** Returns ladder codes for a mix, or null if not a mix. */
    fun getMixByCode(code: String): LadderMixDef?

    /** Returns summary info for all configured ladder mixes. */
    fun listAllMixes(): List<LadderMixSummary>

    data class LadderSummary(val code: String, val name: String?, val levelCount: Int)

    data class LadderMixDef(val mixCode: String, val ladderCodes: List<String>)

    data class LadderMixSummary(val code: String, val name: String?, val ladderCodes: List<String>)
}
