package app.antidoomscroll.domain

import java.util.UUID

/**
 * Pure domain: an exercise with prompt, type, difficulty, and scoring params.
 * Belongs to one Subject. No JPA — infrastructure maps to/from this.
 * For N_BACK type, exerciseParams contains: n, sequence, matchIndices.
 */
data class Exercise(
    val id: UUID,
    val subjectId: UUID,
    val type: ExerciseType,
    val difficulty: Difficulty,
    val prompt: String,
    val expectedAnswers: List<String>,
    val timeLimitSeconds: Int,
    val exerciseParams: Map<String, Any?>? = null
) {
    fun isUltraEasy(): Boolean = difficulty == Difficulty.ULTRA_EASY

    fun nBackParams(): NBackParams? {
        return when (type) {
            ExerciseType.N_BACK -> {
                val p = exerciseParams ?: return null
                val n = (p["n"] as? Number)?.toInt() ?: return null
                @Suppress("UNCHECKED_CAST")
                val sequence = (p["sequence"] as? List<*>)?.map { it.toString() } ?: return null
                @Suppress("UNCHECKED_CAST")
                val matchIndices = (p["matchIndices"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: return null
                NBackParams(n = n, sequence = sequence, matchIndices = matchIndices)
            }
            else -> null
        }
    }
}
