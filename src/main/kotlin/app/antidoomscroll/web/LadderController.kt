package app.antidoomscroll.web

import app.antidoomscroll.application.port.LadderPort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Discovery endpoint: returns all configured ladders.
 * Enables the frontend to render a dynamic ladder selection screen
 * without any hardcoded ladder codes.
 */
@RestController
@RequestMapping("/api/ladders")
class LadderController(private val ladderPort: LadderPort) {

    @GetMapping
    fun listLadders(): List<LadderSummaryDto> =
        ladderPort.listAll().map { LadderSummaryDto(it.code, it.name, it.levelCount) }

    data class LadderSummaryDto(val code: String, val name: String?, val levelCount: Int)
    data class LadderMixSummaryDto(val code: String, val name: String?, val ladderCodes: List<String>)
}
