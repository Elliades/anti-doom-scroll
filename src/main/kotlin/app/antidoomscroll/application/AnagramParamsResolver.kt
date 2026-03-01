package app.antidoomscroll.application

import app.antidoomscroll.domain.Difficulty
import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.ExerciseType
import app.antidoomscroll.domain.AnagramParams

object AnagramParamsResolver {

    fun resolve(exercise: Exercise): AnagramParams? = runCatching {
        if (exercise.type != ExerciseType.ANAGRAM) return@runCatching null
        val p = exercise.exerciseParams
        val language = (p?.get("language") as? String)?.lowercase() ?: "fr"
        val (minLetters, maxLetters) = when (exercise.difficulty) {
            Difficulty.ULTRA_EASY -> 2 to 3
            Difficulty.EASY -> 3 to 4
            Difficulty.MEDIUM -> 4 to 5
            Difficulty.HARD -> 6 to 7
            Difficulty.VERY_HARD -> 8 to 15
        }
        AnagramParams(minLetters = minLetters, maxLetters = maxLetters, language = language)
    }.getOrNull()
}
