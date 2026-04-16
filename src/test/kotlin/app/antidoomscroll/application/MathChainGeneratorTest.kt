package app.antidoomscroll.application

import app.antidoomscroll.domain.ArithmeticComplexity
import app.antidoomscroll.domain.Difficulty
import app.antidoomscroll.domain.MathOperation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

class MathChainGeneratorTest {

    private val generator = MathChainGenerator()

    @Test
    fun `ultra easy generates 2 steps with add or subtract only`() {
        val rng = Random(42)
        repeat(20) {
            val result = generator.generate(Difficulty.ULTRA_EASY, rng)
            assertEquals(2, result.steps.size)
            assertTrue(result.steps.all { it.operation in listOf(MathOperation.ADD, MathOperation.SUBTRACT) })
            assertTrue(result.startNumber in 1..9) { "Start should be 1 digit: ${result.startNumber}" }
            assertTrue(result.expectedAnswer > 0) { "Expected answer should be positive" }
            verifyChainArithmetic(result.startNumber, result.steps, result.expectedAnswer)
        }
    }

    @Test
    fun `easy generates 2-3 steps with add or subtract only`() {
        val rng = Random(123)
        repeat(20) {
            val result = generator.generate(Difficulty.EASY, rng)
            assertTrue(result.steps.size in 2..3) { "Expected 2-3 steps, got ${result.steps.size}" }
            assertTrue(result.steps.all { it.operation in listOf(MathOperation.ADD, MathOperation.SUBTRACT) })
            assertTrue(result.expectedAnswer > 0)
            verifyChainArithmetic(result.startNumber, result.steps, result.expectedAnswer)
        }
    }

    @Test
    fun `medium generates 3-4 steps and may include multiply or divide`() {
        val rng = Random(7)
        repeat(20) {
            val result = generator.generate(Difficulty.MEDIUM, rng)
            assertTrue(result.steps.size in 3..4) { "Expected 3-4 steps, got ${result.steps.size}" }
            assertTrue(result.expectedAnswer > 0)
            verifyChainArithmetic(result.startNumber, result.steps, result.expectedAnswer)
        }
    }

    @Test
    fun `hard generates 4-5 steps`() {
        val rng = Random(999)
        repeat(20) {
            val result = generator.generate(Difficulty.HARD, rng)
            assertTrue(result.steps.size in 4..5) { "Expected 4-5 steps, got ${result.steps.size}" }
            assertTrue(result.expectedAnswer > 0)
            verifyChainArithmetic(result.startNumber, result.steps, result.expectedAnswer)
        }
    }

    @Test
    fun `very hard generates 5-7 steps`() {
        val rng = Random(2025)
        repeat(20) {
            val result = generator.generate(Difficulty.VERY_HARD, rng)
            assertTrue(result.steps.size in 5..7) { "Expected 5-7 steps, got ${result.steps.size}" }
            assertTrue(result.expectedAnswer > 0)
            verifyChainArithmetic(result.startNumber, result.steps, result.expectedAnswer)
        }
    }

    @Test
    fun `all difficulties produce positive expected answers`() {
        val rng = Random(2026)
        for (diff in Difficulty.entries) {
            repeat(15) {
                val result = generator.generate(diff, rng)
                assertTrue(result.expectedAnswer > 0) { "$diff: expected positive answer, got ${result.expectedAnswer}" }
                assertTrue(result.totalComplexity >= 0.0)
            }
        }
    }

    @Test
    fun `step complexity is non-negative for all steps`() {
        val rng = Random(314)
        for (diff in Difficulty.entries) {
            repeat(10) {
                val result = generator.generate(diff, rng)
                for (step in result.steps) {
                    assertTrue(step.complexity >= 0.0) { "$diff step ${step.operation} ${step.operand}: negative complexity" }
                }
            }
        }
    }

    @Test
    fun `intermediate results stay within digit bounds for ultra easy`() {
        val cfg = MathChainGenerator.configFor(Difficulty.ULTRA_EASY)
        val rng = Random(555)
        repeat(30) {
            val result = generator.generate(Difficulty.ULTRA_EASY, rng)
            var current = result.startNumber
            for (step in result.steps) {
                current = applyOp(current, step.operation, step.operand)
                val digits = ArithmeticComplexity.digits(current)
                assertTrue(digits <= cfg.maxIntermediateDigits) {
                    "Intermediate $current has $digits digits, max is ${cfg.maxIntermediateDigits}"
                }
                assertTrue(current > 0) { "Intermediate result should be positive: $current" }
            }
        }
    }

    @Test
    fun `per-step complexity stays within max for each difficulty`() {
        val rng = Random(77)
        for (diff in Difficulty.entries) {
            val cfg = MathChainGenerator.configFor(diff)
            repeat(10) {
                val result = generator.generate(diff, rng)
                for (step in result.steps) {
                    assertTrue(step.complexity <= cfg.maxStepComplexity + 0.1) {
                        "$diff: step complexity ${step.complexity} exceeds max ${cfg.maxStepComplexity}"
                    }
                }
            }
        }
    }

    @Test
    fun `division steps produce clean results`() {
        val rng = Random(2024)
        for (diff in listOf(Difficulty.MEDIUM, Difficulty.HARD, Difficulty.VERY_HARD)) {
            repeat(30) {
                val result = generator.generate(diff, rng)
                var current = result.startNumber
                for (step in result.steps) {
                    if (step.operation == MathOperation.DIVIDE) {
                        assertEquals(0, current % step.operand) {
                            "$diff: $current ÷ ${step.operand} is not clean"
                        }
                    }
                    current = applyOp(current, step.operation, step.operand)
                }
            }
        }
    }

    @Test
    fun `total complexity is sum of step complexities`() {
        val rng = Random(42)
        val result = generator.generate(Difficulty.MEDIUM, rng)
        val expectedTotal = result.steps.sumOf { it.complexity }
        assertEquals(expectedTotal, result.totalComplexity, 0.001)
    }

    @Test
    fun `harder difficulties tend to have more steps than easier ones`() {
        val rng = Random(1)
        val ultraEasySteps = (0 until 50).map { generator.generate(Difficulty.ULTRA_EASY, rng).steps.size }.average()
        val hardSteps = (0 until 50).map { generator.generate(Difficulty.HARD, rng).steps.size }.average()
        assertTrue(hardSteps > ultraEasySteps) {
            "Hard ($hardSteps avg steps) should have more steps than ultra easy ($ultraEasySteps)"
        }
    }

    private fun verifyChainArithmetic(start: Int, steps: List<app.antidoomscroll.domain.MathChainStep>, expected: Int) {
        var current = start
        for (step in steps) {
            current = applyOp(current, step.operation, step.operand)
        }
        assertEquals(expected, current) { "Chain arithmetic mismatch: computed $current, expected $expected" }
    }

    private fun applyOp(a: Int, op: MathOperation, b: Int): Int = when (op) {
        MathOperation.ADD -> a + b
        MathOperation.SUBTRACT -> a - b
        MathOperation.MULTIPLY -> a * b
        MathOperation.DIVIDE -> if (b != 0) a / b else 0
    }
}
