package app.antidoomscroll.application.port

import app.antidoomscroll.domain.LadderConfig

/**
 * Port: load ladder configuration.
 */
interface LadderPort {

    fun getByCode(code: String): LadderConfig?
}
