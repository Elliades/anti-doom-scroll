package app.antidoomscroll.domain

/**
 * Params for ANAGRAM exercise. Letter range per difficulty:
 * ULTRA_EASY: 2–3, EASY: 3–4, MEDIUM: 4–5, HARD: 5+
 */
data class AnagramParams(
    val minLetters: Int,
    val maxLetters: Int,
    val language: String
) {
    init {
        require(minLetters in 2..15) { "minLetters must be 2–15" }
        require(maxLetters in 2..15) { "maxLetters must be 2–15" }
        require(minLetters <= maxLetters) { "minLetters must be <= maxLetters" }
    }
}
