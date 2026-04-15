package app.antidoomscroll.application

import app.antidoomscroll.application.port.LadderPort
import app.antidoomscroll.application.port.ProfilePort
import app.antidoomscroll.application.port.SubjectPort
import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.LadderMixState
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Start a ladder mix session: alternates exercises between multiple ladders.
 * Returns first exercise from the first ladder at level 0.
 */
@Service
class StartLadderMixSessionUseCase(
    private val ladderPort: LadderPort,
    private val ladderExercisePicker: LadderExercisePicker,
    private val subjectPort: SubjectPort,
    private val profilePort: ProfilePort
) {

    fun start(profileIdOpt: String?, mixCode: String): LadderMixSessionResult {
        val profile = profileIdOpt
            ?.takeIf { it.isNotBlank() }
            ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?.let { profilePort.findById(it) }
            ?: profilePort.getOrCreateAnonymousProfile()

        val mixDef = ladderPort.getMixByCode(mixCode)
            ?: throw IllegalArgumentException("Ladder mix not found: $mixCode")

        val ladderCodes = mixDef.ladderCodes
        val perLadderStates = ladderCodes.associateWith {
            LadderMixState.PerLadderState(
                recentScores = emptyList(),
                overallScoreSum = 0.0,
                overallTotal = 0
            )
        }

        val state = LadderMixState(
            mixCode = mixCode,
            ladderCodes = ladderCodes,
            currentLevelIndex = 0,
            perLadderStates = perLadderStates,
            nextLadderIndex = 0
        )

        val firstLadderCode = state.nextLadderCode()
        val config = ladderPort.getByCode(firstLadderCode)
            ?: throw IllegalStateException("Ladder config not found: $firstLadderCode")
        val level0 = config.levelAt(0)
            ?: throw IllegalStateException("Ladder $firstLadderCode has no level 0")

        val exercise = ladderExercisePicker.pick(config, level0)
            ?: throw IllegalStateException("No exercise available for ladder $firstLadderCode level 0")

        val subjectCode = subjectPort.findById(exercise.subjectId)?.code

        val levelCount = ladderCodes.minOfOrNull { code ->
            ladderPort.getByCode(code)?.levels?.size ?: 0
        } ?: 0

        return LadderMixSessionResult(
            profileId = profile.id.toString(),
            exercise = exercise,
            subjectCode = subjectCode,
            ladderMixState = state.withRecentExercisePlayed(exercise.id),
            levelCount = levelCount,
            sessionDefaultSeconds = profile.sessionDefaultSeconds,
            lowBatteryModeSeconds = profile.lowBatteryModeSeconds
        )
    }

    data class LadderMixSessionResult(
        val profileId: String,
        val exercise: Exercise,
        val subjectCode: String?,
        val ladderMixState: LadderMixState,
        val levelCount: Int,
        val sessionDefaultSeconds: Int,
        val lowBatteryModeSeconds: Int
    )
}
