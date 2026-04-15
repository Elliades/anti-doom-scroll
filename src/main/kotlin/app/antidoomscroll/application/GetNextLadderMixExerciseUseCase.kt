package app.antidoomscroll.application

import app.antidoomscroll.application.port.LadderPort
import app.antidoomscroll.application.port.SubjectPort
import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.LadderMixState
import org.springframework.stereotype.Service

/**
 * Process a completed exercise in a ladder mix and return the next one.
 * Alternates between ladders.
 *
 * With 2 ladders: advance when both pass (≥75%); demote if either fails (<40%).
 * With 3+ ladders: require only 3 scores per ladder to evaluate (so we check after ~3×N exercises).
 * Advance when at least (N-1) ladders pass, so one weak ladder does not block; demote if any fails (<40%).
 */
@Service
class GetNextLadderMixExerciseUseCase(
    private val ladderPort: LadderPort,
    private val ladderExercisePicker: LadderExercisePicker,
    private val subjectPort: SubjectPort
) {

    /** When mix has 3+ ladders, we require only this many scores per ladder to evaluate (so level can advance sooner). */
    private val requiredScoresPerLadderWhenMany = 3

    /**
     * @param lastCompletedLadderCode the ladder we just finished
     * @param lastScore score 0–1 from the completed exercise
     */
    fun getNext(
        state: LadderMixState,
        lastCompletedLadderCode: String,
        lastScore: Double
    ): LadderMixNextResult? {
        if (lastCompletedLadderCode !in state.ladderCodes) return null

        val config = ladderPort.getByCode(lastCompletedLadderCode) ?: return null
        val thresholds = config.thresholds
        val n = state.ladderCodes.size

        // Store up to max threshold scores per ladder; for evaluation we use per-level thresholds
        val maxThreshold = state.ladderCodes.maxOfOrNull { code ->
            ladderPort.getByCode(code)?.thresholds?.answersNeededToAdvance ?: 5
        } ?: 5
        var newState = state.withScoreAdded(lastCompletedLadderCode, lastScore, maxThreshold)
        var levelChanged: GetNextLadderExerciseUseCase.LevelChange? = null

        // Check each ladder individually based on its own per-level threshold
        // For 3+ ladders, cap at requiredScoresPerLadderWhenMany (3) to evaluate sooner
        val allHaveEnough = newState.ladderCodes.all { code ->
            val ladderConfig = ladderPort.getByCode(code) ?: return@all false
            val perLevelThreshold = ladderConfig.getAnswersNeededToAdvance(state.currentLevelIndex)
            // For 3+ ladders, cap at 3 to allow earlier evaluation
            val requiredForThisLadder = if (n > 2) {
                minOf(perLevelThreshold, requiredScoresPerLadderWhenMany)
            } else {
                perLevelThreshold
            }
            (newState.perLadderStates[code]?.recentScores?.size ?: 0) >= requiredForThisLadder
        }
        if (allHaveEnough) {
            val maxLevel = newState.ladderCodes.minOfOrNull { code ->
                ladderPort.getByCode(code)?.levels?.maxOfOrNull { it.levelIndex } ?: 0
            } ?: 0

            val passingCount = newState.ladderCodes.count { code ->
                (newState.perLadderStates[code]?.averageRecent() ?: 0.0) >= thresholds.minScoreToAdvance
            }
            // With 2 ladders: both must pass. With 3+: at least (n-1) must pass so one weak ladder does not block.
            val enoughPass = if (n <= 2) passingCount == n else passingCount >= n - 1

            val anyFail = newState.ladderCodes.any { code ->
                val avg = newState.perLadderStates[code]?.averageRecent() ?: 1.0
                avg < thresholds.minScoreToStay
            }

            when {
                enoughPass && newState.currentLevelIndex < maxLevel -> {
                    levelChanged = GetNextLadderExerciseUseCase.LevelChange(
                        newState.currentLevelIndex,
                        newState.currentLevelIndex + 1,
                        "up"
                    )
                    newState = newState.withLevelAdvanced()
                }
                anyFail && newState.currentLevelIndex > 0 -> {
                    levelChanged = GetNextLadderExerciseUseCase.LevelChange(
                        newState.currentLevelIndex,
                        newState.currentLevelIndex - 1,
                        "down"
                    )
                    newState = newState.withLevelDemoted()
                }
            }
        }

        newState = newState.withNextLadder()
        val nextLadderCode = newState.nextLadderCode()
        val nextConfig = ladderPort.getByCode(nextLadderCode) ?: return null
        val levelIndex = newState.currentLevelIndex.coerceAtMost(
            (nextConfig.levels.maxOfOrNull { it.levelIndex } ?: 0)
        )
        val level = nextConfig.levelAt(levelIndex) ?: return null
        val exercise = ladderExercisePicker.pick(nextConfig, level)
        val subjectCode = exercise?.let { subjectPort.findById(it.subjectId)?.code }

        return LadderMixNextResult(
            exercise = exercise,
            subjectCode = subjectCode,
            ladderMixState = newState,
            levelChanged = levelChanged
        )
    }

    data class LadderMixNextResult(
        val exercise: Exercise?,
        val subjectCode: String? = null,
        val ladderMixState: LadderMixState,
        val levelChanged: GetNextLadderExerciseUseCase.LevelChange?
    )
}
