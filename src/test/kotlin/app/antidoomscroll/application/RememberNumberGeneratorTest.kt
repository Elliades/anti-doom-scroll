package app.antidoomscroll.application

import app.antidoomscroll.domain.Difficulty
import app.antidoomscroll.domain.RememberNumberParams
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.random.Random

class RememberNumberGeneratorTest {

    private val mathGenerator = MathFlashcardGenerator()
    private val generator = RememberNumberGenerator(mathGenerator)

    @Test
    fun `generates 2-digit number for numberDigits=2`() {
        val params = RememberNumberParams(numberDigits = 2, displayTimeMs = 3000, mathOperation = "ADD", mathFirstMax = 9, mathSecondMax = 9)
        val rng = Random(42)
        repeat(20) {
            val result = generator.generate(params, Difficulty.ULTRA_EASY, rng)
            assertTrue(result.numberToRemember in 10..99) { "Expected 2-digit number, got ${result.numberToRemember}" }
            assertEquals(3000, result.displayTimeMs)
            assertTrue(result.mathPrompt.startsWith("What is ")) { "Math prompt format: ${result.mathPrompt}" }
            assertTrue(result.mathExpectedAnswer.toIntOrNull() != null) { "Math answer should be numeric" }
        }
    }

    @Test
    fun `generates 5-digit number for numberDigits=5`() {
        val params = RememberNumberParams(numberDigits = 5, displayTimeMs = 1500, mathOperation = "MULTIPLY", mathFirstMax = 12, mathSecondMax = 9)
        val rng = Random(99)
        repeat(20) {
            val result = generator.generate(params, Difficulty.HARD, rng)
            assertTrue(result.numberToRemember in 10000..99999) { "Expected 5-digit number, got ${result.numberToRemember}" }
            assertEquals(1500, result.displayTimeMs)
        }
    }

    @Test
    fun `generates different numbers across calls`() {
        val params = RememberNumberParams(numberDigits = 3)
        val numbers = (1..10).map { generator.generate(params, null).numberToRemember }.toSet()
        assertTrue(numbers.size > 1) { "Should generate varying numbers, got: $numbers" }
    }

    @Test
    fun `math prompt uses correct operation`() {
        val paramsAdd = RememberNumberParams(numberDigits = 2, mathOperation = "ADD", mathFirstMax = 50, mathSecondMax = 50)
        val resultAdd = generator.generate(paramsAdd, null, Random(1))
        assertTrue(resultAdd.mathPrompt.contains("+")) { "ADD prompt should contain +: ${resultAdd.mathPrompt}" }

        val paramsSub = RememberNumberParams(numberDigits = 2, mathOperation = "SUBTRACT", mathFirstMax = 50, mathSecondMax = 50)
        val resultSub = generator.generate(paramsSub, null, Random(2))
        // Subtraction uses − (not -)
        assertTrue(resultSub.mathPrompt.contains("−") || resultSub.mathPrompt.contains("-")) {
            "SUBTRACT prompt should contain minus: ${resultSub.mathPrompt}"
        }
    }

    @Test
    fun `complexity score is non-null`() {
        val params = RememberNumberParams(numberDigits = 3, mathOperation = "ADD", mathFirstMax = 50, mathSecondMax = 50)
        val result = generator.generate(params, Difficulty.EASY, Random(7))
        assertNotNull(result.mathComplexityScore) { "Complexity score should be set" }
        assertTrue(result.mathComplexityScore!! >= 0.0) { "Complexity must be non-negative" }
    }
}
