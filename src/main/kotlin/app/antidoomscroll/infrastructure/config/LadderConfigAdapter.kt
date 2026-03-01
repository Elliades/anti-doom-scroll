package app.antidoomscroll.infrastructure.config

import app.antidoomscroll.application.port.LadderPort
import app.antidoomscroll.config.LadderConfigProperties
import app.antidoomscroll.domain.LadderConfig
import org.springframework.stereotype.Component

@Component
class LadderConfigAdapter(
    private val properties: LadderConfigProperties
) : LadderPort {

    override fun getByCode(code: String): LadderConfig? =
        properties.ladderByCode(code)

    override fun listAll(): List<LadderPort.LadderSummary> =
        properties.ladders.map { (code, def) ->
            LadderPort.LadderSummary(
                code = code,
                name = def.name,
                levelCount = def.levels.size
            )
        }
}
