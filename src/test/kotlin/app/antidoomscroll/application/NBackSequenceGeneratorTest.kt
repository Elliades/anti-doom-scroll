package app.antidoomscroll.application

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NBackSequenceGeneratorTest {

    @Test
    fun `generate with suitCount 1 uses only clubs`() {
        val (sequence, matchIndices) = NBackSequenceGenerator.generate(n = 1, suitCount = 1, sequenceLength = 12, seed = 42)
        assertEquals(12, sequence.size)
        assertEquals(matchIndices.size, matchIndices.count { it >= 1 && it < sequence.size })
        assertTrue(matchIndices.all { sequence[it] == sequence[it - 1] })
        assertTrue(sequence.all { it.endsWith("C") }) { "suitCount=1 should use clubs only: $sequence" }
    }

    @Test
    fun `generate with suitCount 2 uses C and D`() {
        val (sequence, matchIndices) = NBackSequenceGenerator.generate(n = 1, suitCount = 2, sequenceLength = 12, seed = 123)
        assertEquals(12, sequence.size)
        assertTrue(matchIndices.all { sequence[it] == sequence[it - 1] })
        val suits = sequence.map { it.last() }.toSet()
        assertTrue(suits.all { it in "CD" }) { "suitCount=2 should use C,D: $sequence" }
    }

    @Test
    fun `generate with n=2 produces valid 2-back matches`() {
        val (sequence, matchIndices) = NBackSequenceGenerator.generate(n = 2, suitCount = 3, sequenceLength = 15, seed = 999)
        assertEquals(15, sequence.size)
        assertTrue(matchIndices.all { it >= 2 && it < sequence.size })
        assertTrue(matchIndices.all { sequence[it] == sequence[it - 2] })
    }

    @Test
    fun `same seed produces same sequence`() {
        val a = NBackSequenceGenerator.generate(n = 1, suitCount = 1, sequenceLength = 10, seed = 7)
        val b = NBackSequenceGenerator.generate(n = 1, suitCount = 1, sequenceLength = 10, seed = 7)
        assertEquals(a.first, b.first)
        assertEquals(a.second, b.second)
    }
}
