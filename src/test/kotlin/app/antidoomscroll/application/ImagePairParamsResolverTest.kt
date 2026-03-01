package app.antidoomscroll.application

import app.antidoomscroll.domain.Difficulty
import app.antidoomscroll.domain.Exercise
import app.antidoomscroll.domain.ExerciseType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.UUID

class ImagePairParamsResolverTest {

    private val subjectId = UUID.fromString("b0000000-0000-0000-0000-000000000008")

    @Test
    fun `resolves pairCount maxPairsPerBackground colorCount from exercise_params`() {
        val exercise = Exercise(
            id = UUID.randomUUID(),
            subjectId = subjectId,
            type = ExerciseType.IMAGE_PAIR,
            difficulty = Difficulty.EASY,
            prompt = "Find pairs",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 120,
            exerciseParams = mapOf(
                "pairCount" to 4,
                "maxPairsPerBackground" to 2,
                "colorCount" to 1
            )
        )
        val params = ImagePairParamsResolver.resolve(exercise)
        assertNotNull(params)
        assertEquals(4, params!!.pairCount)
        assertEquals(2, params.maxPairsPerBackground)
        assertEquals(1, params.colorCount)
    }

    @Test
    fun `defaults maxPairsPerBackground and colorCount when omitted`() {
        val exercise = Exercise(
            id = UUID.randomUUID(),
            subjectId = subjectId,
            type = ExerciseType.IMAGE_PAIR,
            difficulty = Difficulty.MEDIUM,
            prompt = "Match",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 180,
            exerciseParams = mapOf("pairCount" to 4)
        )
        val params = ImagePairParamsResolver.resolve(exercise)
        assertNotNull(params)
        assertEquals(4, params!!.pairCount)
        assertEquals(2, params.maxPairsPerBackground)
        assertEquals(1, params.colorCount)
    }

    @Test
    fun `returns null for non IMAGE_PAIR type`() {
        val exercise = Exercise(
            id = UUID.randomUUID(),
            subjectId = subjectId,
            type = ExerciseType.MEMORY_CARD_PAIRS,
            difficulty = Difficulty.EASY,
            prompt = "Pairs",
            expectedAnswers = emptyList(),
            timeLimitSeconds = 60,
            exerciseParams = mapOf("pairCount" to 4, "symbols" to listOf("a", "b", "c", "d"))
        )
        assertEquals(null, ImagePairParamsResolver.resolve(exercise))
    }
}
