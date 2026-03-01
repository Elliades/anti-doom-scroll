package app.antidoomscroll.domain

/**
 * Score thresholds for ladder advancement.
 * Evaluated over the last [answersNeededToAdvance] answers.
 */
data class LadderThresholds(
    /** Minimum score (0–1) to stay in level. Below this demotes (if possible). */
    val minScoreToStay: Double = 0.40,
    /** Minimum score (0–1) to advance to next level. */
    val minScoreToAdvance: Double = 0.75,
    /** Number of recent answers used to evaluate advancement. */
    val answersNeededToAdvance: Int = 5
) {
    init {
        require(minScoreToStay in 0.0..1.0) { "minScoreToStay must be 0–1" }
        require(minScoreToAdvance in 0.0..1.0) { "minScoreToAdvance must be 0–1" }
        require(minScoreToAdvance >= minScoreToStay) { "minScoreToAdvance must be >= minScoreToStay" }
        require(answersNeededToAdvance >= 1) { "answersNeededToAdvance must be >= 1" }
    }
}
