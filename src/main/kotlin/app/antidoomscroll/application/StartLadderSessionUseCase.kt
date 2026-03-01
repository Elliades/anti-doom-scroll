package app.antidoomscroll.application

import app.antidoomscroll.application.port.ExercisePort
import app.antidoomscroll.application.port.LadderPort
import app.antidoomscroll.application.port.ProfilePort
import app.antidoomscroll.application.port.SubjectPort
import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.LadderConfig
import app.antidoomscroll.domain.LadderLevel
import app.antidoomscroll.domain.LadderState
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Start a ladder session: returns first exercise from level 0 and initial ladder state.
 */
@Service
class StartLadderSessionUseCase(
    private val ladderPort: LadderPort,
    private val exercisePort: ExercisePort,
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
        val exercise = pickExerciseForLevel(config, level0)
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
            sessionDefaultSeconds = profile.sessionDefaultSeconds,
            lowBatteryModeSeconds = profile.lowBatteryModeSeconds
        )
    }

    internal fun pickExerciseForLevel(config: LadderConfig, level: LadderLevel): Exercise? {
        val subjectCode = level.subjectCode ?: "default"
        val subject = subjectPort.findByCode(subjectCode) ?: return null

        val candidates = if (level.exerciseIds != null && level.exerciseIds.isNotEmpty()) {
            exercisePort.findByIds(level.exerciseIds)
        } else {
            exercisePort.findRandomBySubjectAndDifficulties(
                subject.id,
                level.allowedDifficulties,
                10
            )
        }
        val filtered = if (level.mathOperations != null && level.mathOperations.isNotEmpty()) {
            val opSet = level.mathOperations.toSet()
            candidates.filter { ex ->
                ex.mathFlashcardParams()?.operation in opSet
            }
        } else {
            candidates
        }
        return filtered.randomOrNull()
    }

    data class LadderSessionResult(
        val profileId: String,
        val exercise: Exercise,
        val subjectCode: String?,
        val ladderState: LadderState,
        val sessionDefaultSeconds: Int,
        val lowBatteryModeSeconds: Int
    )
}
