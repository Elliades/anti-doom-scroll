package app.antidoomscroll.web

import app.antidoomscroll.application.port.LadderPort
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Dedicated endpoint for listing ladder mixes (GET /api/ladders/mixes).
 * Separate controller avoids path-matching ambiguity with LadderController in Spring Boot 3.
 */
@RestController
@RequestMapping("/api/ladders/mixes")
class LadderMixController(private val ladderPort: LadderPort) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun list(): List<LadderController.LadderMixSummaryDto> =
        ladderPort.listAllMixes().map {
            LadderController.LadderMixSummaryDto(it.code, it.name, it.ladderCodes)
        }
}
