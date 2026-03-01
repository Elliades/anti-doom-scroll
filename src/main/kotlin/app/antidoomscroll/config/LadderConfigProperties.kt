package app.antidoomscroll.config

import app.antidoomscroll.domain.Difficulty
import app.antidoomscroll.domain.MathOperation
import app.antidoomscroll.domain.LadderConfig
import app.antidoomscroll.domain.LadderLevel
import app.antidoomscroll.domain.LadderThresholds
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import java.util.UUID

@ConfigurationProperties(prefix = "app.ladder")
data class LadderConfigProperties(
    @DefaultValue("default") val defaultCode: String,
    val ladders: Map<String, LadderDef> = emptyMap()
) {
    data class LadderDef(
        val name: String? = null,
        val thresholds: ThresholdsDef = ThresholdsDef(),
        val levels: List<LevelDef> = emptyList()
    ) {
        data class ThresholdsDef(
            val minScoreToStay: Double = 0.40,
            val minScoreToAdvance: Double = 0.75,
            val answersNeededToAdvance: Int = 5
        )

        data class LevelDef(
            val levelIndex: Int,
            val allowedDifficulties: List<String> = emptyList(),
            val subjectCode: String? = null,
            val exerciseIds: List<String>? = null,
            val mathOperations: List<String>? = null
        )
    }

    fun ladderByCode(code: String): LadderConfig? {
        val def = ladders[code] ?: return null
        val levels = def.levels.map { l ->
            LadderLevel(
                levelIndex = l.levelIndex,
                allowedDifficulties = l.allowedDifficulties.map { Difficulty.valueOf(it) },
                subjectCode = l.subjectCode ?: "default",
                exerciseIds = l.exerciseIds?.mapNotNull { runCatching { UUID.fromString(it) }.getOrNull() }?.takeIf { it.isNotEmpty() },
                mathOperations = l.mathOperations?.map { MathOperation.valueOf(it) }?.takeIf { it.isNotEmpty() }
            )
        }
        return LadderConfig(
            code = code,
            name = def.name,
            levels = levels,
            thresholds = LadderThresholds(
                minScoreToStay = def.thresholds.minScoreToStay,
                minScoreToAdvance = def.thresholds.minScoreToAdvance,
                answersNeededToAdvance = def.thresholds.answersNeededToAdvance
            )
        )
    }
}
