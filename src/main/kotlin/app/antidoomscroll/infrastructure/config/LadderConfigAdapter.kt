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

    override fun getMixByCode(code: String): LadderPort.LadderMixDef? {
        val ladderCodes = properties.mixes[code] ?: return null
        if (ladderCodes.size < 2) return null
        return LadderPort.LadderMixDef(mixCode = code, ladderCodes = ladderCodes)
    }

    override fun listAllMixes(): List<LadderPort.LadderMixSummary> =
        properties.mixes
            .filter { it.value.size >= 2 }
            .map { (code, ladderCodes) ->
                val name = if (code == "mix" || ladderCodes.size > 2) "Ladder Mix" else {
                    ladderCodes.mapNotNull { properties.ladders[it]?.name }.joinToString(" + ").takeIf { it.isNotBlank() }
                }
                LadderPort.LadderMixSummary(
                    code = code,
                    name = name,
                    ladderCodes = ladderCodes
                )
            }
}
