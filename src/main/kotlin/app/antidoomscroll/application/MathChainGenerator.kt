package app.antidoomscroll.application

import app.antidoomscroll.domain.ArithmeticComplexity
import app.antidoomscroll.domain.Difficulty
import app.antidoomscroll.domain.MathChainParams
import app.antidoomscroll.domain.MathChainStep
import app.antidoomscroll.domain.MathOperation
import org.springframework.stereotype.Component
import kotlin.random.Random

/**
 * Generates MATH_CHAIN exercises: a starting number followed by N sequential operations.
 *
 * Design philosophy: prefer more simple steps over fewer complex ones.
 * Example: 5×5+60-10×2 is better than 1972-356/2.
 *
 * Each step is evaluated with [ArithmeticComplexity] to ensure individual operations
 * fit the difficulty, and intermediate results have digit counts matching the target range.
 */
@Component
class MathChainGenerator {

    private val maxRetries = 120

    fun generate(difficulty: Difficulty, random: Random = Random.Default): MathChainParams {
        val cfg = configFor(difficulty)
        repeat(maxRetries) {
            val result = tryGenerate(cfg, random)
            if (result != null) return result
        }
        return generateFallback(cfg, random)
    }

    private fun tryGenerate(cfg: ChainConfig, random: Random): MathChainParams? {
        val stepCount = cfg.minSteps + random.nextInt((cfg.maxSteps - cfg.minSteps + 1).coerceAtLeast(1))
        val startNumber = randomInDigitRange(cfg.startMinDigits, cfg.startMaxDigits, random)
        var current = startNumber
        val steps = mutableListOf<MathChainStep>()
        var totalComplexity = 0.0

        for (i in 0 until stepCount) {
            val step = pickStep(current, cfg, random) ?: return null
            current = applyOp(current, step.operation, step.operand)
            if (current <= 0 || ArithmeticComplexity.digits(current) > cfg.maxIntermediateDigits) return null
            if (ArithmeticComplexity.digits(current) < cfg.minIntermediateDigits) return null
            steps.add(step)
            totalComplexity += step.complexity
        }

        if (current <= 0) return null
        return MathChainParams(
            startNumber = startNumber,
            steps = steps,
            expectedAnswer = current,
            totalComplexity = totalComplexity
        )
    }

    private fun pickStep(current: Int, cfg: ChainConfig, random: Random): MathChainStep? {
        val ops = cfg.allowedOps.shuffled(random)
        for (op in ops) {
            repeat(30) {
                val operand = pickOperand(current, op, cfg, random) ?: return@repeat
                val complexity = complexityFor(current, op, operand) ?: return@repeat
                if (complexity > cfg.maxStepComplexity) return@repeat
                val result = applyOp(current, op, operand)
                if (result <= 0) return@repeat
                val resultDigits = ArithmeticComplexity.digits(result)
                if (resultDigits < cfg.minIntermediateDigits || resultDigits > cfg.maxIntermediateDigits) return@repeat
                return MathChainStep(op, operand, complexity)
            }
        }
        return null
    }

    private fun pickOperand(current: Int, op: MathOperation, cfg: ChainConfig, random: Random): Int? {
        return when (op) {
            MathOperation.ADD -> {
                val max = operandMaxForDigitCap(current, cfg.maxIntermediateDigits)
                if (max < 1) null else 1 + random.nextInt(max.coerceAtLeast(1))
            }
            MathOperation.SUBTRACT -> {
                val floor = digitFloor(cfg.minIntermediateDigits)
                val maxSub = (current - floor).coerceAtLeast(0)
                if (maxSub < 1) null else 1 + random.nextInt(maxSub.coerceAtLeast(1))
            }
            MathOperation.MULTIPLY -> {
                val maxProduct = digitCeiling(cfg.maxIntermediateDigits)
                val maxMul = (maxProduct / current.coerceAtLeast(1)).coerceAtLeast(1)
                if (maxMul < 2) null else 2 + random.nextInt((maxMul - 1).coerceAtLeast(1))
            }
            MathOperation.DIVIDE -> {
                val divisors = (2..current / 2).filter { current % it == 0 }
                    .filter { ArithmeticComplexity.digits(current / it) in cfg.minIntermediateDigits..cfg.maxIntermediateDigits }
                divisors.randomOrNull(random)
            }
        }
    }

    private fun complexityFor(current: Int, op: MathOperation, operand: Int): Double? {
        return when (op) {
            MathOperation.ADD -> ArithmeticComplexity.complexityAdd(current, operand)
            MathOperation.SUBTRACT -> {
                if (operand > current) return null
                ArithmeticComplexity.complexitySubtract(current, operand)
            }
            MathOperation.MULTIPLY -> ArithmeticComplexity.complexityMultiply(current, operand)
            MathOperation.DIVIDE -> {
                if (operand <= 0 || current % operand != 0) return null
                ArithmeticComplexity.complexityDivide(current, operand)
            }
        }
    }

    private fun applyOp(a: Int, op: MathOperation, b: Int): Int = when (op) {
        MathOperation.ADD -> a + b
        MathOperation.SUBTRACT -> a - b
        MathOperation.MULTIPLY -> a * b
        MathOperation.DIVIDE -> if (b != 0) a / b else 0
    }

    @Suppress("UNUSED_PARAMETER")
    private fun generateFallback(cfg: ChainConfig, random: Random): MathChainParams {
        val start = randomInDigitRange(1, 1, random)
        val operand = 1 + random.nextInt(9)
        val step = MathChainStep(
            MathOperation.ADD, operand,
            ArithmeticComplexity.complexityAdd(start, operand)
        )
        return MathChainParams(
            startNumber = start,
            steps = listOf(step),
            expectedAnswer = start + operand,
            totalComplexity = step.complexity
        )
    }

    private fun randomInDigitRange(minDigits: Int, maxDigits: Int, random: Random): Int {
        val lo = digitFloor(minDigits)
        val hi = digitCeiling(maxDigits)
        return lo + random.nextInt((hi - lo + 1).coerceAtLeast(1))
    }

    private fun digitFloor(digits: Int): Int = when (digits) {
        1 -> 1; 2 -> 10; 3 -> 100; 4 -> 1000; else -> 1
    }

    private fun digitCeiling(digits: Int): Int = when (digits) {
        1 -> 9; 2 -> 99; 3 -> 999; 4 -> 9999; else -> 9
    }

    private fun operandMaxForDigitCap(current: Int, maxDigits: Int): Int {
        val cap = digitCeiling(maxDigits)
        return (cap - current).coerceAtLeast(0)
    }

    companion object {
        fun configFor(difficulty: Difficulty): ChainConfig = when (difficulty) {
            Difficulty.ULTRA_EASY -> ChainConfig(
                minSteps = 2, maxSteps = 2,
                allowedOps = listOf(MathOperation.ADD, MathOperation.SUBTRACT),
                startMinDigits = 1, startMaxDigits = 1,
                minIntermediateDigits = 1, maxIntermediateDigits = 2,
                maxStepComplexity = 5.0
            )
            Difficulty.EASY -> ChainConfig(
                minSteps = 2, maxSteps = 3,
                allowedOps = listOf(MathOperation.ADD, MathOperation.SUBTRACT),
                startMinDigits = 1, startMaxDigits = 2,
                minIntermediateDigits = 1, maxIntermediateDigits = 2,
                maxStepComplexity = 10.0
            )
            Difficulty.MEDIUM -> ChainConfig(
                minSteps = 3, maxSteps = 4,
                allowedOps = MathOperation.entries,
                startMinDigits = 1, startMaxDigits = 2,
                minIntermediateDigits = 1, maxIntermediateDigits = 3,
                maxStepComplexity = 15.0
            )
            Difficulty.HARD -> ChainConfig(
                minSteps = 4, maxSteps = 5,
                allowedOps = MathOperation.entries,
                startMinDigits = 1, startMaxDigits = 2,
                minIntermediateDigits = 1, maxIntermediateDigits = 3,
                maxStepComplexity = 20.0
            )
            Difficulty.VERY_HARD -> ChainConfig(
                minSteps = 5, maxSteps = 7,
                allowedOps = MathOperation.entries,
                startMinDigits = 1, startMaxDigits = 3,
                minIntermediateDigits = 1, maxIntermediateDigits = 4,
                maxStepComplexity = 30.0
            )
        }
    }
}

data class ChainConfig(
    val minSteps: Int,
    val maxSteps: Int,
    val allowedOps: List<MathOperation>,
    val startMinDigits: Int,
    val startMaxDigits: Int,
    val minIntermediateDigits: Int,
    val maxIntermediateDigits: Int,
    val maxStepComplexity: Double
)
