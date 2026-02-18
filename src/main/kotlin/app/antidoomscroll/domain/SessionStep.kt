package app.antidoomscroll.domain

/**
 * One step in a session: ultra-easy, then easy/medium, then optional hard.
 */
data class SessionStep(
    val stepIndex: Int,
    val difficulty: Difficulty,
    val exercise: Exercise
) {
    companion object {
        fun of(stepIndex: Int, exercise: Exercise) =
            SessionStep(stepIndex, exercise.difficulty, exercise)
    }
}
