package app.antidoomscroll.config

import app.antidoomscroll.domain.Difficulty
import app.antidoomscroll.domain.ExerciseType
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
            /** One or more subject codes to pool candidates from. Empty = all subjects. */
            val subjectCodes: List<String> = emptyList(),
            val exerciseIds: List<String>? = null,
            /**
             * Generic exercise param filter: key → allowed values (any-match).
             * Example: {operation: [ADD, SUBTRACT]}
             */
            val exerciseParamFilter: Map<String, List<String>>? = null,
            /** Optional gate on ExerciseType (e.g. [N_BACK]). */
            val allowedTypes: List<String>? = null
        )
    }

    fun ladderByCode(code: String): LadderConfig? {
        val def = ladders[code] ?: return null
        val levels = def.levels.map { l ->
            LadderLevel(
                levelIndex = l.levelIndex,
                allowedDifficulties = l.allowedDifficulties.map { Difficulty.valueOf(it) },
                subjectCodes = l.subjectCodes,
                exerciseIds = l.exerciseIds
                    ?.mapNotNull { runCatching { UUID.fromString(it) }.getOrNull() }
                    ?.takeIf { it.isNotEmpty() },
                exerciseParamFilter = l.exerciseParamFilter?.takeIf { it.isNotEmpty() },
                allowedTypes = l.allowedTypes
                    ?.map { ExerciseType.valueOf(it) }
                    ?.takeIf { it.isNotEmpty() }
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
