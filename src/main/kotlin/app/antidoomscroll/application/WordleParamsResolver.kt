package app.antidoomscroll.application

import app.antidoomscroll.domain.Difficulty
import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.ExerciseType
import app.antidoomscroll.domain.WordleParams

object WordleParamsResolver {

    /**
     * Resolves WordleParams from an exercise.
     * wordLength per difficulty: EASY=3, MEDIUM=5, HARD=6, VERY_HARD=7.
     * language is stored in exercise_params (fr or en).
     */
    fun resolve(exercise: Exercise): WordleParams? = runCatching {
        if (exercise.type != ExerciseType.WORDLE) return@runCatching null
        val p = exercise.exerciseParams
        val language = (p?.get("language") as? String)?.lowercase() ?: "fr"
        val maxAttempts = (p?.get("maxAttempts") as? Number)?.toInt() ?: 6
        val wordLength = when (exercise.difficulty) {
            Difficulty.EASY -> 3
            Difficulty.MEDIUM -> 5
            Difficulty.HARD -> 6
            Difficulty.VERY_HARD -> 7
            else -> 3
        }
        WordleParams(wordLength = wordLength, language = language, maxAttempts = maxAttempts)
    }.getOrNull()
}
