package app.antidoomscroll.application

import app.antidoomscroll.domain.MathFlashcardParams
import app.antidoomscroll.domain.MathOperation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random

class MathFlashcardGeneratorTest {

    private val generator = MathFlashcardGenerator()

    @Test
    fun `multiply with firstValues uses fixed multiplier and produces valid product`() {
        val params = MathFlashcardParams(
            operation = MathOperation.MULTIPLY,
            firstMin = 1,
            firstMax = 10,
            secondMin = 1,
            secondMax = 9,
            firstValues = listOf(2, 5, 10)
        )
        val random = Random(42)
        repeat(20) {
            val (prompt, answer) = generator.generate(params, random)
            assertTrue(prompt.startsWith("What is ")) { prompt }
            assertTrue(prompt.contains(" × ")) { prompt }
            assertTrue(prompt.endsWith("?")) { prompt }
            val parts = prompt.removePrefix("What is ").removeSuffix("?").split(" × ")
            assertEquals(2, parts.size)
            val a = parts[0].trim().toInt()
            val b = parts[1].trim().toInt()
            assertTrue(a in listOf(2, 5, 10)) { "First factor should be 2, 5, or 10: $a" }
            assertTrue(b in 1..9) { "Second factor should be 1-9: $b" }
            assertEquals((a * b).toString(), answer)
        }
    }

    @Test
    fun `multiply easy range 1-9 times 1-12`() {
        val params = MathFlashcardParams(
            operation = MathOperation.MULTIPLY,
            firstMin = 1,
            firstMax = 9,
            secondMin = 1,
            secondMax = 12
        )
        val (prompt, answer) = generator.generate(params, Random(123))
        assertTrue(prompt.contains(" × "))
        val parts = prompt.removePrefix("What is ").removeSuffix("?").split(" × ")
        val a = parts[0].trim().toInt()
        val b = parts[1].trim().toInt()
        assertTrue(a in 1..9)
        assertTrue(b in 1..12)
        assertEquals((a * b).toString(), answer)
    }

    @Test
    fun `divide with secondValues uses fixed divisor and clean quotient`() {
        val params = MathFlashcardParams(
            operation = MathOperation.DIVIDE,
            firstMin = 1,
            firstMax = 9,
            secondMax = 10,
            secondValues = listOf(2, 5, 10)
        )
        val random = Random(7)
        repeat(20) {
            val (prompt, answer) = generator.generate(params, random)
            assertTrue(prompt.startsWith("What is ")) { prompt }
            assertTrue(prompt.contains(" ÷ ")) { prompt }
            val parts = prompt.removePrefix("What is ").removeSuffix("?").split(" ÷ ")
            assertEquals(2, parts.size)
            val dividend = parts[0].trim().toInt()
            val divisor = parts[1].trim().toInt()
            assertTrue(divisor in listOf(2, 5, 10)) { "Divisor should be 2, 5, or 10: $divisor" }
            val quotient = answer.toInt()
            assertTrue(quotient in 1..9)
            assertEquals(dividend, divisor * quotient)
        }
    }

    @Test
    fun `divide easy range quotient 1-12 divisor 2-9`() {
        val params = MathFlashcardParams(
            operation = MathOperation.DIVIDE,
            firstMin = 1,
            firstMax = 12,
            secondMin = 2,
            secondMax = 9
        )
        val (prompt, answer) = generator.generate(params, Random(99))
        assertTrue(prompt.contains(" ÷ "))
        val parts = prompt.removePrefix("What is ").removeSuffix("?").split(" ÷ ")
        val dividend = parts[0].trim().toInt()
        val divisor = parts[1].trim().toInt()
        assertTrue(divisor in 2..9)
        val q = answer.toInt()
        assertTrue(q in 1..12)
        assertEquals(dividend, divisor * q)
    }

    @Test
    fun `add and subtract unchanged`() {
        val addParams = MathFlashcardParams(operation = MathOperation.ADD, firstMax = 9, secondMax = 9)
        val (addPrompt, addAnswer) = generator.generate(addParams, Random(1))
        assertTrue(addPrompt.contains(" + "))
        val addParts = addPrompt.removePrefix("What is ").removeSuffix("?").split(" + ")
        assertEquals((addParts[0].trim().toInt() + addParts[1].trim().toInt()).toString(), addAnswer)

        val subParams = MathFlashcardParams(operation = MathOperation.SUBTRACT, firstMax = 20, secondMax = 9)
        val (subPrompt, subAnswer) = generator.generate(subParams, Random(2))
        assertTrue(subPrompt.contains(" − "))
        val subParts = subPrompt.removePrefix("What is ").removeSuffix("?").split(" − ")
        val minuend = subParts[0].trim().toInt()
        val subtrahend = subParts[1].trim().toInt()
        assertTrue(subtrahend <= minuend)
        assertEquals((minuend - subtrahend).toString(), subAnswer)
    }

    @Test
    fun `MathFlashcardParams rejects empty firstValues`() {
        assertThrows<IllegalArgumentException> {
            MathFlashcardParams(
                operation = MathOperation.MULTIPLY,
                firstMax = 9,
                secondMax = 9,
                firstValues = emptyList()
            )
        }
    }

    @Test
    fun `MathFlashcardParams rejects empty secondValues`() {
        assertThrows<IllegalArgumentException> {
            MathFlashcardParams(
                operation = MathOperation.DIVIDE,
                firstMin = 1,
                firstMax = 9,
                secondMax = 10,
                secondValues = emptyList()
            )
        }
    }
}
