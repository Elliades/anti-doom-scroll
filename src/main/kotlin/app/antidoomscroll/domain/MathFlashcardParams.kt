package app.antidoomscroll.domain

/**
 * Params for generated math flashcards (FLASHCARD_QA with exercise_params).
 * Used when prompt/expectedAnswers are generated at response time.
 *
 * Digit ranges: 1 digit = 1–9, 2 digits = 10–99, 3 digits = 100–999, 4 digits = 1000–9999.
 * firstMax/secondMax define the max value for each operand (inclusive).
 *
 * Optional value sets (for multiply/divide difficulty progressions):
 * - firstValues: if set, first operand is chosen from this list (e.g. [2, 5, 10] for "× 2, 5, or 10").
 * - secondValues: if set, second operand is chosen from this list (e.g. [2, 5, 10] for "÷ 2, 5, or 10").
 */
data class MathFlashcardParams(
    val operation: MathOperation,
    val firstMin: Int = 1,
    val firstMax: Int,
    val secondMin: Int = 1,
    val secondMax: Int,
    val firstValues: List<Int>? = null,
    val secondValues: List<Int>? = null
) {
    init {
        if (firstValues == null) {
            require(firstMin <= firstMax) { "firstMin must be <= firstMax" }
        } else {
            require(firstValues.isNotEmpty()) { "firstValues must not be empty" }
        }
        if (secondValues == null) {
            require(secondMin <= secondMax) { "secondMin must be <= secondMax" }
        } else {
            require(secondValues.isNotEmpty()) { "secondValues must not be empty" }
        }
    }
}

enum class MathOperation {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE
}
