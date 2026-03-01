package app.antidoomscroll.domain

/**
 * Params for WORDLE exercise.
 * wordLength per difficulty: EASY=3, MEDIUM=5, HARD=6, VERY_HARD=7.
 * language: "fr" (French) or "en" (English).
 */
data class WordleParams(
    val wordLength: Int,
    val language: String,
    val maxAttempts: Int = 6
) {
    init {
        require(wordLength in 3..10) { "wordLength must be 3–10" }
        require(maxAttempts in 3..10) { "maxAttempts must be 3–10" }
    }
}
