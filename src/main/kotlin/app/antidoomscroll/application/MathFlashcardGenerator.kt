package app.antidoomscroll.application

import app.antidoomscroll.domain.MathFlashcardParams
import app.antidoomscroll.domain.MathOperation
import org.springframework.stereotype.Component
import kotlin.random.Random

/**
 * Generates (prompt, expectedAnswer) for math flashcards.
 * Each call produces a new random problem.
 */
@Component
class MathFlashcardGenerator {

    fun generate(params: MathFlashcardParams, random: Random = Random.Default): Pair<String, String> {
        return when (params.operation) {
            MathOperation.ADD -> generateAdd(params, random)
            MathOperation.SUBTRACT -> generateSubtract(params, random)
            MathOperation.MULTIPLY -> generateMultiply(params, random)
            MathOperation.DIVIDE -> generateDivide(params, random)
        }
    }

    private fun generateAdd(params: MathFlashcardParams, random: Random): Pair<String, String> {
        val a = params.firstMin + random.nextInt(params.firstMax - params.firstMin + 1)
        val b = params.secondMin + random.nextInt(params.secondMax - params.secondMin + 1)
        return "What is $a + $b?" to (a + b).toString()
    }

    private fun generateSubtract(params: MathFlashcardParams, random: Random): Pair<String, String> {
        val a = params.firstMin + random.nextInt(params.firstMax - params.firstMin + 1)
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
