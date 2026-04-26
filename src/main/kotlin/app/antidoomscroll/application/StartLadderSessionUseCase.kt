package app.antidoomscroll.application

import app.antidoomscroll.application.port.LadderPort
import app.antidoomscroll.application.port.ProfilePort
import app.antidoomscroll.application.port.SubjectPort
import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.LadderState
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Start a ladder session: returns first exercise from level 0 and initial ladder state.
 */
@Service
class StartLadderSessionUseCase(
    private val ladderPort: LadderPort,
    private val ladderExercisePicker: LadderExercisePicker,
    private val subjectPort: SubjectPort,
    private val profilePort: ProfilePort
) {

    fun start(profileIdOpt: String?, ladderCode: String): LadderSessionResult {
        val profile = profileIdOpt
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?.let { profilePort.findById(it) }
            ?: profilePort.getOrCreateAnonymousProfile()

        val config = ladderPort.getByCode(ladderCode)
            ?: ladderPort.getByCode("default")
            ?: throw IllegalArgumentException("Ladder config not found: $ladderCode")

        val level0 = config.levelAt(0) ?: throw IllegalStateException("Ladder has no level 0")
        val targetScore = ladderExercisePicker.targetScoreForLevel(config, level0)
        val exercise = ladderExercisePicker.pick(config, level0, targetScore = targetScore)
            ?: throw IllegalStateException("No exercise available for ladder level 0")

        val subjectCode = subjectPort.findById(exercise.subjectId)?.code

        return LadderSessionResult(
            profileId = profile.id.toString(),
            exercise = exercise,
            subjectCode = subjectCode,
            ladderState = LadderState(
                ladderCode = config.code,
                currentLevelIndex = 0,
                recentScores = emptyList(),
                overallScoreSum = 0.0,
                overallTotal = 0
            ),
            levelCount = config.levels.size,
            sessionDefaultSeconds = profile.sessionDefaultSeconds,
            lowBatteryModeSeconds = profile.lowBatteryModeSeconds
        )
    }

    data class LadderSessionResult(
        val profileId: String,
        val exercise: Exercise,
        val subjectCode: String?,
        val ladderState: LadderState,
        val levelCount: Int,
        val sessionDefaultSeconds: Int,
        val lowBatteryModeSeconds: Int
    )
}
