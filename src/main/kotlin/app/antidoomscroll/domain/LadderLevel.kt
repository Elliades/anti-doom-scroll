package app.antidoomscroll.domain

import java.util.UUID

/**
 * Definition of one level in a ladder.
 * Exercises are selected by [allowedDifficulties] and optionally [subjectCode].
 * For FLASHCARD_QA math: [mathOperations] filters by ADD, SUBTRACT, MULTIPLY, DIVIDE.
 * Or [exerciseIds] for a fixed set of exercises.
 */
data class LadderLevel(
    val levelIndex: Int,
    val allowedDifficulties: List<Difficulty>,
    val subjectCode: String? = null,
    val exerciseIds: List<UUID>? = null,
    /** For math ladders: filter FLASHCARD_QA by operation (ADD, SUBTRACT, MULTIPLY, DIVIDE). */
    val mathOperations: List<MathOperation>? = null
) {
    init {
        require(levelIndex >= 0) { "levelIndex must be >= 0" }
        if (exerciseIds == null || exerciseIds.isEmpty()) {
            require(allowedDifficulties.isNotEmpty()) { "allowedDifficulties required when no exerciseIds" }
        }
    }
}
