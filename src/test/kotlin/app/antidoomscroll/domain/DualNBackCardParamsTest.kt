package app.antidoomscroll.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DualNBackCardParamsTest {

    @Test
    fun `evaluate returns correct score for number match`() {
        val params = DualNBackCardParams(
            n = 1,
            sequence = listOf("AC", "2D", "2C", "3H"),
            matchColorIndices = listOf(),
            matchNumberIndices = listOf(2)
        )
        val score = params.evaluate(userColorResponses = emptySet(), userNumberResponses = setOf(2))
        assertEquals(1, score.positionHits)
        assertEquals(0, score.colorMisses)
        assertEquals(1.0, score.combinedNormalizedScore())
    }

    @Test
    fun `evaluate handles color match`() {
        val params = DualNBackCardParams(
            n = 1,
            sequence = listOf("AC", "2D", "AC", "3H"),
            matchColorIndices = listOf(2),
            matchNumberIndices = listOf()
        )
        val score = params.evaluate(userColorResponses = setOf(2), userNumberResponses = emptySet())
        assertEquals(1, score.colorHits)
        assertEquals(0, score.positionHits)
    }
}
