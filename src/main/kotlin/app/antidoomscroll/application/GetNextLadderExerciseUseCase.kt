package app.antidoomscroll.application

import app.antidoomscroll.application.port.LadderPort
import app.antidoomscroll.application.port.SubjectPort
import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.LadderState
import org.springframework.stereotype.Service

/**
 * Process a completed exercise and return the next one with updated ladder state.
 * Applies advancement logic: 75%+ advance, <40% demote, else stay.
 */
@Service
class GetNextLadderExerciseUseCase(
    private val ladderPort: LadderPort,
    private val ladderExercisePicker: LadderExercisePicker,
    private val subjectPort: SubjectPort
) {

    /**
     * @param lastScore score 0–1 from the completed exercise
     * @return next exercise and updated state, or null if config invalid
     */
    fun getNext(state: LadderState, lastScore: Double): LadderNextResult? {
        val config = ladderPort.getByCode(state.ladderCode) ?: return null
        val thresholds = config.thresholds

        var newState = state.withScoreAdded(lastScore, thresholds.answersNeededToAdvance)
        var levelChanged: LevelChange? = null

        val recent = newState.recentScores
        val canEvaluate = recent.size >= thresholds.answersNeededToAdvance
        if (canEvaluate) {
            val avg = recent.average()
            val maxLevel = config.levels.maxOfOrNull { it.levelIndex } ?: 0

            when {
                avg >= thresholds.minScoreToAdvance && newState.currentLevelIndex < maxLevel -> {
                    levelChanged = LevelChange(newState.currentLevelIndex, newState.currentLevelIndex + 1, "up")
                    newState = newState.withLevelChanged(newState.currentLevelIndex + 1)
                }
                avg < thresholds.minScoreToStay && newState.currentLevelIndex > 0 -> {
                    levelChanged = LevelChange(newState.currentLevelIndex, newState.currentLevelIndex - 1, "down")
                    newState = newState.withLevelChanged(newState.currentLevelIndex - 1)
                }
            }
        }

        val level = config.levelAt(newState.currentLevelIndex) ?: return null
        val exercise = ladderExercisePicker.pick(config, level)

        val subjectCode = exercise?.let { subjectPort.findById(it.subjectId)?.code }

        return LadderNextResult(
            exercise = exercise,
            subjectCode = subjectCode,
            ladderState = newState,
            levelChanged = levelChanged
        )
    }

    data class LadderNextResult(
        val exercise: Exercise?,
        val subjectCode: String? = null,
        val ladderState: LadderState,
        val levelChanged: LevelChange?
    )

    data class LevelChange(val from: Int, val to: Int, val direction: String)
}
