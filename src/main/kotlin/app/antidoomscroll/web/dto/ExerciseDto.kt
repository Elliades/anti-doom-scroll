package app.antidoomscroll.web.dto

import java.util.UUID

/**
 * Immutable DTO for exercise in API responses.
 * Exercises belong to a subject (subjectId / subjectCode).
 * For N_BACK type, nBackParams contains sequence and matchIndices.
 */
data class ExerciseDto(
    val id: UUID,
    val subjectId: UUID,
    val subjectCode: String?,
    val type: String,
    val difficulty: String,
    val prompt: String,
    val expectedAnswers: List<String>,
    val timeLimitSeconds: Int,
    val nBackParams: NBackParamsDto? = null
)

data class NBackParamsDto(
    val n: Int,
    val sequence: List<String>,
    val matchIndices: List<Int>
)
