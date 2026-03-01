package app.antidoomscroll.application

import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.ExerciseType
import app.antidoomscroll.domain.ImagePairParams

/**
 * Resolves ImagePairParams from an Exercise. Expects exercise_params:
 * - pairCount: Int (number of pairs, >= 2)
 * - maxPairsPerBackground: Int (optional, default 2; max pairs sharing same background)
 * - colorCount: Int (optional, default 1; 0 = no bg color, 1 = no color + 1 color, etc.)
 */
object ImagePairParamsResolver {

    fun resolve(exercise: Exercise): ImagePairParams? = runCatching {
        if (exercise.type != ExerciseType.IMAGE_PAIR) return@runCatching null
        val p = exercise.exerciseParams ?: return@runCatching null
        val pairCount = (p["pairCount"] as? Number)?.toInt() ?: return@runCatching null
        val maxPairsPerBackground = (p["maxPairsPerBackground"] as? Number)?.toInt() ?: 2
        val colorCount = (p["colorCount"] as? Number)?.toInt() ?: 1
        ImagePairParams(
            pairCount = pairCount,
            maxPairsPerBackground = maxPairsPerBackground,
            colorCount = colorCount
        )
    }.getOrNull()
}
