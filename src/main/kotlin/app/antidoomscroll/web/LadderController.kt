package app.antidoomscroll.web

import app.antidoomscroll.application.port.LadderPort
import app.antidoomscroll.config.LadderConfigProperties
import app.antidoomscroll.web.dto.LadderOfflineBundleDto
import app.antidoomscroll.web.dto.LadderOfflineMixDto
import app.antidoomscroll.web.dto.LadderOfflineSummaryDto
import org.springframework.http.MediaType
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
class LadderController(
    private val ladderPort: LadderPort,
    private val ladderConfigProperties: LadderConfigProperties
) {

    @GetMapping
    fun listLadders(): List<LadderSummaryDto> =
        ladderPort.listAll().map { LadderSummaryDto(it.code, it.name, it.levelCount) }

    /** YAML ladder definitions for regenerating `ladder-offline.json` on the frontend. */
    @GetMapping("/offline-bundle", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun offlineBundle(): LadderOfflineBundleDto =
        LadderOfflineBundleDto(
            ladders = ladderPort.listAll().map {
                LadderOfflineSummaryDto(it.code, it.name, it.levelCount)
            },
            mixes = ladderPort.listAllMixes().map {
                LadderOfflineMixDto(it.code, it.name, it.ladderCodes)
            },
            configs = ladderConfigProperties.ladders
        )

    data class LadderSummaryDto(val code: String, val name: String?, val levelCount: Int)
    data class LadderMixSummaryDto(val code: String, val name: String?, val ladderCodes: List<String>)
}
