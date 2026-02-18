package app.antidoomscroll.web.dto

/**
 * One step in a session (ultra-easy, easy/medium, optional hard).
 */
data class SessionStepDto(
    val stepIndex: Int,
    val difficulty: String,
    val exercise: ExerciseDto
)
