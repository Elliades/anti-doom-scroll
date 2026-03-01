package app.antidoomscroll.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NBackGridParamsTest {

    @Test
    fun `evaluate returns correct score for hits and false alarms`() {
        val params = NBackGridParams(
            n = 1,
            sequence = listOf(0, 4, 2, 4, 8),
            matchIndices = listOf(3),
            gridSize = 3
        )
        val score = params.evaluate(setOf(3))
        assertEquals(1, score.hits)
        assertEquals(0, score.misses)
        assertEquals(0, score.falseAlarms)
        assertEquals(1.0, score.normalizedScore())
    }

    @Test
    fun `evaluate penalizes false alarms`() {
        val params = NBackGridParams(
            n = 1,
            sequence = listOf(0, 4, 2, 4, 8),
            matchIndices = listOf(3),
            gridSize = 3
        )
        val score = params.evaluate(setOf(3, 0, 1, 2))
        assertEquals(1, score.hits)
        assertEquals(3, score.falseAlarms)
        assertTrue(score.normalizedScore() < 1.0)
    }

    @Test
    fun `rejects invalid grid positions`() {
        assertThrows<IllegalArgumentException> {
            NBackGridParams(
                n = 1,
                sequence = listOf(0, 4, 9),
                matchIndices = listOf(2),
                gridSize = 3
            )
        }
    }
}
