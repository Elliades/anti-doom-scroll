package app.antidoomscroll.application

import app.antidoomscroll.domain.Difficulty
import app.antidoomscroll.domain.MathFlashcardParams
import app.antidoomscroll.domain.MathOperation
import app.antidoomscroll.domain.RememberNumberParams
import org.springframework.stereotype.Component
import kotlin.random.Random

data class RememberNumberResult(
    val numberToRemember: Int,
    val displayTimeMs: Int,
    val mathPrompt: String,
    val mathExpectedAnswer: String,
    val mathComplexityScore: Double?
)

/**
 * Generates REMEMBER_NUMBER exercise content at response time:
 * a random N-digit number to memorize and a math distraction problem.
 */
@Component
class RememberNumberGenerator(
    private val mathFlashcardGenerator: MathFlashcardGenerator
) {

    fun generate(
        params: RememberNumberParams,
        difficulty: Difficulty?,
        random: Random = Random.Default
    ): RememberNumberResult {
        val number = generateNumber(params.numberDigits, random)
        val operation = parseMathOperation(params.mathOperation)
        val mathParams = MathFlashcardParams(
            operation = operation,
            firstMin = 1,
            firstMax = params.mathFirstMax,
            secondMin = 1,
            secondMax = params.mathSecondMax
        )
        val mathResult = mathFlashcardGenerator.generate(mathParams, difficulty, random)
        return RememberNumberResult(
            numberToRemember = number,
            displayTimeMs = params.displayTimeMs,
            mathPrompt = mathResult.prompt,
            mathExpectedAnswer = mathResult.expectedAnswer,
            mathComplexityScore = mathResult.complexityScore
        )
    }

    private fun generateNumber(digits: Int, random: Random): Int {
        val min = tenPow(digits - 1)
        val max = tenPow(digits) - 1
        return min + random.nextInt(max - min + 1)
    }

    private fun tenPow(n: Int): Int {
        var result = 1
        repeat(n) { result *= 10 }
        return result
    }

    private fun parseMathOperation(op: String): MathOperation = when (op.uppercase()) {
        "SUBTRACT" -> MathOperation.SUBTRACT
        "MULTIPLY" -> MathOperation.MULTIPLY
        "DIVIDE" -> MathOperation.DIVIDE
        else -> MathOperation.ADD
    }
}
