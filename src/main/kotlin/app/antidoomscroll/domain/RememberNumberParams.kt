package app.antidoomscroll.domain

/**
 * DB-stored params for REMEMBER_NUMBER exercise.
 *
 * The exercise flow: show a random N-digit number → math distraction → recall.
 * The generator uses these params to produce the number and distraction problem
 * at response time.
 *
 * - numberDigits: how many digits in the number to memorize (2–7)
 * - displayTimeMs: how long the number is shown (ms)
 * - mathOperation: distraction operation (ADD, SUBTRACT, MULTIPLY, DIVIDE)
 * - mathFirstMax / mathSecondMax: operand bounds for the math problem
 */
data class RememberNumberParams(
    val numberDigits: Int,
    val displayTimeMs: Int = 3000,
    val mathOperation: String = "ADD",
    val mathFirstMax: Int = 9,
    val mathSecondMax: Int = 9
) {
    init {
        require(numberDigits in 2..7) { "numberDigits must be 2–7" }
        require(displayTimeMs in 500..10_000) { "displayTimeMs must be 500–10 000" }
    }
}
