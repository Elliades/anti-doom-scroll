package app.antidoomscroll.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DualNBackGridParamsTest {

    @Test
    fun `evaluate returns correct dual score`() {
        val params = DualNBackGridParams(
            n = 1,
            sequence = listOf(
                GridStimulus(0, "#4285F4"),
                GridStimulus(4, "#EA4335"),
                GridStimulus(0, "#FBBC04")
            ),
            matchPositionIndices = listOf(2),
            matchColorIndices = listOf(),
            colors = listOf("#4285F4", "#EA4335", "#FBBC04", "#34A853"),
            gridSize = 3
        )
        val score = params.evaluate(userPositionResponses = setOf(2), userColorResponses = emptySet())
        assertEquals(1, score.positionHits)
        assertEquals(0, score.positionMisses)
        assertEquals(1.0, score.combinedNormalizedScore())
    }

    @Test
    fun `evaluate handles both position and color matches`() {
        val params = DualNBackGridParams(
            n = 1,
            sequence = listOf(
                GridStimulus(0, "#4285F4"),
                GridStimulus(4, "#EA4335"),
                GridStimulus(0, "#4285F4")
            ),
            matchPositionIndices = listOf(2),
            matchColorIndices = listOf(2),
            colors = listOf("#4285F4", "#EA4335"),
            gridSize = 3
        )
        val score = params.evaluate(
            userPositionResponses = setOf(2),
            userColorResponses = setOf(2)
        )
        assertEquals(1, score.positionHits)
        assertEquals(1, score.colorHits)
        assertTrue(score.combinedNormalizedScore() > 0.9)
    }
}
