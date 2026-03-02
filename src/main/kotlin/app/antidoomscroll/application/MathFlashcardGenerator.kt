package app.antidoomscroll.application

import app.antidoomscroll.domain.ArithmeticComplexity
import app.antidoomscroll.domain.Difficulty
import app.antidoomscroll.domain.MathFlashcardParams
import app.antidoomscroll.domain.MathOperation
import org.springframework.stereotype.Component
import kotlin.random.Random

/** Result of math flashcard generation: prompt, expected answer, and human complexity score. */
data class MathFlashcardResult(
    val prompt: String,
    val expectedAnswer: String,
    val complexityScore: Double
)

/**
 * Generates (prompt, expectedAnswer) for math flashcards.
 * When [difficulty] is provided, uses generate-then-filter so the problem's complexity
 * falls within the difficulty band (see [ArithmeticComplexity.scoreBandFor]).
 */
@Component
class MathFlashcardGenerator {

    private val maxTriesForBand = 80

    /**
     * Generates a problem and its complexity score. If [difficulty] is set, tries to pick
     * operands whose complexity falls in the band; otherwise returns the first generated problem.
     */
    fun generate(
        params: MathFlashcardParams,
        difficulty: Difficulty? = null,
        random: Random = Random.Default
    ): MathFlashcardResult {
        val (minScore, maxScore) = difficulty?.let { ArithmeticComplexity.scoreBandFor(it) } ?: (0.0 to Double.MAX_VALUE)
        repeat(maxTriesForBand) {
            val (prompt, answer) = generateOne(params, random)
            val score = complexityFor(params.operation, prompt, answer)
            if (score in minScore..maxScore) return MathFlashcardResult(prompt, answer, score)
        }
        val (prompt, answer) = generateOne(params, random)
        val score = complexityFor(params.operation, prompt, answer)
        return MathFlashcardResult(prompt, answer, score)
    }

    /** Legacy: returns (prompt, expectedAnswer) without complexity. */
    fun generate(params: MathFlashcardParams, random: Random): Pair<String, String> {
        val r = generate(params, null, random)
        return r.prompt to r.expectedAnswer
    }

    private fun complexityFor(operation: MathOperation, prompt: String, answer: String): Double {
        val (a, b) = parseOperands(prompt, operation) ?: return 0.0
        return when (operation) {
            MathOperation.ADD -> ArithmeticComplexity.complexityAdd(a, b)
            MathOperation.SUBTRACT -> ArithmeticComplexity.complexitySubtract(a, b)
            MathOperation.MULTIPLY -> ArithmeticComplexity.complexityMultiply(a, b)
            MathOperation.DIVIDE -> ArithmeticComplexity.complexityDivide(a, b)
        }
    }

    private fun parseOperands(prompt: String, operation: MathOperation): Pair<Int, Int>? {
        val q = prompt.removePrefix("What is ").removeSuffix("?")
        return when (operation) {
            MathOperation.ADD -> q.split(" + ").takeIf { it.size == 2 }?.let { (x, y) -> x.trim().toIntOrNull() to y.trim().toIntOrNull() }?.let { (a, b) -> if (a != null && b != null) a to b else null }
            MathOperation.SUBTRACT -> q.split(" − ").takeIf { it.size == 2 }?.let { (x, y) -> x.trim().toIntOrNull() to y.trim().toIntOrNull() }?.let { (a, b) -> if (a != null && b != null) a to b else null }
            MathOperation.MULTIPLY -> q.split(" × ").takeIf { it.size == 2 }?.let { (x, y) -> x.trim().toIntOrNull() to y.trim().toIntOrNull() }?.let { (a, b) -> if (a != null && b != null) a to b else null }
            MathOperation.DIVIDE -> q.split(" ÷ ").takeIf { it.size == 2 }?.let { (x, y) -> x.trim().toIntOrNull() to y.trim().toIntOrNull() }?.let { (a, b) -> if (a != null && b != null) a to b else null }
        }
    }

    private fun generateOne(params: MathFlashcardParams, random: Random): Pair<String, String> = when (params.operation) {
        MathOperation.ADD -> generateAdd(params, random)
        MathOperation.SUBTRACT -> generateSubtract(params, random)
        MathOperation.MULTIPLY -> generateMultiply(params, random)
        MathOperation.DIVIDE -> generateDivide(params, random)
    }

    private fun generateAdd(params: MathFlashcardParams, random: Random): Pair<String, String> {
        val a = params.firstMin + random.nextInt((params.firstMax - params.firstMin + 1).coerceAtLeast(1))
        val b = params.secondMin + random.nextInt((params.secondMax - params.secondMin + 1).coerceAtLeast(1))
        return "What is $a + $b?" to (a + b).toString()
    }

    private fun generateSubtract(params: MathFlashcardParams, random: Random): Pair<String, String> {
        val a = params.firstMin + random.nextInt((params.firstMax - params.firstMin + 1).coerceAtLeast(1))
        val bMax = minOf(a, params.secondMax).coerceAtLeast(params.secondMin)
        val b = params.secondMin + random.nextInt((bMax - params.secondMin + 1).coerceAtLeast(1))
        return "What is $a − $b?" to (a - b).toString()
    }

    private fun generateMultiply(params: MathFlashcardParams, random: Random): Pair<String, String> {
        val (a, b) = when {
            params.firstValues != null -> {
                val first = params.firstValues.random(random)
                val second = params.secondMin + random.nextInt((params.secondMax - params.secondMin + 1).coerceAtLeast(1))
                first to second
            }
            params.secondValues != null -> {
                val first = params.firstMin + random.nextInt((params.firstMax - params.firstMin + 1).coerceAtLeast(1))
                val second = params.secondValues.random(random)
                first to second
            }
            else -> {
                val a = params.firstMin + random.nextInt((params.firstMax - params.firstMin + 1).coerceAtLeast(1))
                val b = params.secondMin + random.nextInt((params.secondMax - params.secondMin + 1).coerceAtLeast(1))
                a to b
            }
        }
        return "What is $a × $b?" to (a * b).toString()
    }

    private fun generateDivide(params: MathFlashcardParams, random: Random): Pair<String, String> {
        val (divisor, quotient) = when {
            params.secondValues != null -> {
                val b = params.secondValues.random(random)
                val q = params.firstMin + random.nextInt((params.firstMax - params.firstMin + 1).coerceAtLeast(1))
                b to q
            }
            params.firstValues != null -> {
                val q = params.firstValues.random(random)
                val b = params.secondMin + random.nextInt((params.secondMax - params.secondMin + 1).coerceAtLeast(1))
                b to q
            }
            else -> {
                val b = params.secondMin + random.nextInt((params.secondMax - params.secondMin + 1).coerceAtLeast(1))
                val q = params.firstMin + random.nextInt((params.firstMax - params.firstMin + 1).coerceAtLeast(1))
                b to q
            }
        }
        val dividend = divisor * quotient
        return "What is $dividend ÷ $divisor?" to quotient.toString()
    }
}
