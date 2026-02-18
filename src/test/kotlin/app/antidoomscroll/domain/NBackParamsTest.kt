package app.antidoomscroll.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NBackParamsTest {

    @Test
    fun `evaluate - perfect score when all matches correct`() {
        val params = NBackParams(
            n = 1,
            sequence = listOf("A", "B", "A", "C", "C"),
            matchIndices = listOf(2, 4)
        )
        val score = params.evaluate(setOf(2, 4))
        assertEquals(2, score.hits)
        assertEquals(0, score.misses)
        assertEquals(0, score.falseAlarms)
        assertEquals(1.0, score.accuracy())
        assertEquals(1.0, score.normalizedScore())
    }

    @Test
    fun `evaluate - partial hits`() {
        val params = NBackParams(
            n = 1,
            sequence = listOf("A", "B", "A", "C", "C"),
            matchIndices = listOf(2, 4)
        )
        val score = params.evaluate(setOf(2))
        assertEquals(1, score.hits)
        assertEquals(1, score.misses)
        assertEquals(0, score.falseAlarms)
        assertEquals(0.5, score.accuracy())
    }

    @Test
    fun `evaluate - false alarms incur penalty`() {
        val params = NBackParams(
            n = 1,
            sequence = listOf("A", "B", "A", "C", "C"),
            matchIndices = listOf(2, 4)
        )
        val score = params.evaluate(setOf(2, 4, 0, 1, 3))
        assertEquals(2, score.hits)
        assertEquals(0, score.misses)
        assertEquals(3, score.falseAlarms)
        assertEquals(1.0, score.accuracy())
        assert(score.normalizedScore() < 1.0)
    }

    @Test
    fun `evaluate - no harsh punishment caps penalty`() {
        val params = NBackParams(
            n = 1,
            sequence = listOf("A", "B", "A", "C", "C"),
            matchIndices = listOf(2, 4)
        )
        val score = params.evaluate(setOf(0, 1, 3))
        assertEquals(0, score.hits)
        assertEquals(2, score.misses)
        assertEquals(3, score.falseAlarms)
        assertEquals(0.0, score.accuracy())
        val ns = score.normalizedScore()
        assert(ns >= 0.0) { "Score must not be negative" }
    }

    @Test
    fun `constructor rejects invalid n`() {
        assertThrows<IllegalArgumentException> {
            NBackParams(
                n = 0,
                sequence = listOf("A", "B"),
                matchIndices = emptyList()
            )
        }
    }

    @Test
    fun `constructor rejects too short sequence`() {
        assertThrows<IllegalArgumentException> {
            NBackParams(
                n = 1,
                sequence = listOf("A"),
                matchIndices = emptyList()
            )
        }
    }
}
